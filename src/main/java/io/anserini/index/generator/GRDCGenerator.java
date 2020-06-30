package io.anserini.index.generator;

import com.fasterxml.jackson.databind.JsonNode;
import io.anserini.analysis.DefaultEnglishAnalyzer;
import io.anserini.collection.GRDCBaseDocument;
import io.anserini.collection.GRDCstreamerCollection;
import io.anserini.index.IndexArgs;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

import java.io.StringReader;

public class GRDCGenerator implements LuceneDocumentGenerator<GRDCBaseDocument> {
    private IndexArgs args;

    public enum GRDCFields {
        SHA("sha"), SOURCE("source_x"), DOI("doi"), TITLE("title"), AUTHORS("authors"), AUTHOR_STRING("author_string"),
        ABSTRACT("abstract"), JOURNAL("journal"), PUBLISH_TIME("publish_time"), YEAR("year"), LICENSE("license"),
        PMC_ID("pmcid"), PUBMED_ID("pubmed_id"), MICROSOFT_ID("mag_id"), S2_ID("s2_id"), WHO("who_covidence_id"),
        URL("url");

        public final String name;

        GRDCFields(String s) {
            name = s;
        }
    }

    public enum TrialstreamerField {
        OUTCOMES_VOCAB("outcomes_vocab"), POPULATION_VOCAB("population_vocab"),
        INTERVENTIONS_VOCAB("interventions_vocab");

        public final String name;

        TrialstreamerField(String s) {
            name = s;
        }
    }

    public GRDCGenerator(IndexArgs args) {
        this.args = args;
    }

    @Override
    public Document createDocument(GRDCBaseDocument GRDCDoc) throws GeneratorException {
        String id = GRDCDoc.id();
        String content = GRDCDoc.contents();
        String raw = GRDCDoc.raw();

        if (id.startsWith("ij3ncdb6") || id.startsWith("c4pt07zk") || id.startsWith("1vimqhdp")
                || id.startsWith("pd1g119c") || id.startsWith("hwjkbpqp") || id.startsWith("gvh0wdxn")) {
            throw new SkippedDocumentException();
        }

        if (content == null || content.trim().isEmpty()) {
            throw new EmptyDocumentException();
        }

        Document doc = new Document();

        // Store the collection docid.
        doc.add(new StringField(IndexArgs.ID, id, Field.Store.YES));
        // This is needed to break score ties by docid.
        doc.add(new SortedDocValuesField(IndexArgs.ID, new BytesRef(id)));

        if (args.storeRaw) {
            doc.add(new StoredField(IndexArgs.RAW, raw));
        }

        FieldType fieldType = new FieldType();
        fieldType.setStored(args.storeContents);

        // Are we storing document vectors?
        if (args.storeDocvectors) {
            fieldType.setStoreTermVectors(true);
            fieldType.setStoreTermVectorPositions(true);
        }

        // Are we building a "positional" or "count" index?
        if (args.storePositions) {
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        } else {
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        }

        doc.add(new Field(IndexArgs.CONTENTS, content, fieldType));

        // normal fields
        doc.add(new Field(GRDCFields.TITLE.name, GRDCDoc.record().get(GRDCFields.TITLE.name), fieldType));
        doc.add(new Field(GRDCFields.ABSTRACT.name, GRDCDoc.record().get(GRDCFields.ABSTRACT.name), fieldType));

        // string fields
        doc.add(new StringField(GRDCFields.SHA.name, GRDCDoc.record().get(GRDCFields.SHA.name), Field.Store.YES));
        doc.add(new StringField(GRDCFields.DOI.name, GRDCDoc.record().get(GRDCFields.DOI.name), Field.Store.YES));
        doc.add(new StringField(GRDCFields.JOURNAL.name, GRDCDoc.record().get(GRDCFields.JOURNAL.name),
                Field.Store.YES));
        doc.add(new StringField(GRDCFields.WHO.name, GRDCDoc.record().get(GRDCFields.WHO.name), Field.Store.YES));
        doc.add(new StringField(GRDCFields.PMC_ID.name, GRDCDoc.record().get(GRDCFields.PMC_ID.name), Field.Store.YES));
        doc.add(new StringField(GRDCFields.PUBMED_ID.name, GRDCDoc.record().get(GRDCFields.PUBMED_ID.name),
                Field.Store.YES));
        doc.add(new StringField(GRDCFields.MICROSOFT_ID.name, GRDCDoc.record().get(GRDCFields.MICROSOFT_ID.name),
                Field.Store.YES));
        doc.add(new StringField(GRDCFields.S2_ID.name, GRDCDoc.record().get(GRDCFields.S2_ID.name), Field.Store.YES));
        doc.add(new StringField(GRDCFields.PUBLISH_TIME.name, GRDCDoc.record().get(GRDCFields.PUBLISH_TIME.name),
                Field.Store.YES));
        doc.add(new StringField(GRDCFields.LICENSE.name, GRDCDoc.record().get(GRDCFields.LICENSE.name),
                Field.Store.YES));

        // default to first URL in metadata
        doc.add(new StringField(GRDCFields.URL.name, GRDCDoc.record().get(GRDCFields.URL.name).split("; ")[0],
                Field.Store.YES));

        if (GRDCDoc instanceof GRDCstreamerCollection.Document) {
            GRDCstreamerCollection.Document tsDoc = (GRDCstreamerCollection.Document) GRDCDoc;
            JsonNode facets = tsDoc.facets();
            addTrialstreamerFacet(doc, TrialstreamerField.OUTCOMES_VOCAB.name, facets);
            addTrialstreamerFacet(doc, TrialstreamerField.POPULATION_VOCAB.name, facets);
            addTrialstreamerFacet(doc, TrialstreamerField.INTERVENTIONS_VOCAB.name, facets);
        }

        // non-stemmed fields
        addAuthors(doc, GRDCDoc.record().get(GRDCFields.AUTHORS.name), fieldType);

        for (String source : GRDCDoc.record().get(GRDCFields.SOURCE.name).split(";")) {
            addNonStemmedField(doc, GRDCFields.SOURCE.name, source.strip(), fieldType);
        }

        // parse year published
        try {
            doc.add(new IntPoint(GRDCFields.YEAR.name,
                    Integer.parseInt(GRDCDoc.record().get(GRDCFields.PUBLISH_TIME.name).strip().substring(0, 4))));
        } catch (Exception e) {
            // can't parse year
        }
        return doc;
    }

    private void addAuthors(Document doc, String authorString, FieldType fieldType) {
        if (authorString == null || authorString == "") {
            return;
        }

        // index raw author string
        addNonStemmedField(doc, GRDCFields.AUTHOR_STRING.name, authorString, fieldType);

        // process all individual author names
        for (String author : authorString.split(";")) {
            addNonStemmedField(doc, GRDCFields.AUTHORS.name, processAuthor(author), fieldType);
        }
    }

    // process author name into a standard order if it is reversed and comma
    // separated
    // eg) Jones, Bob -> Bob Jones
    private String processAuthor(String author) {
        String processedName = "";
        String[] splitNames = author.split(",");
        for (int i = splitNames.length - 1; i >= 0; --i) {
            processedName += splitNames[i].strip() + " ";
        }
        return processedName.strip();
    }

    // indexes a list of facets from the trialstreamer COVID trials dataset
    private void addTrialstreamerFacet(Document doc, String key, JsonNode facets) {
        for (JsonNode value : facets.get(key)) {
            doc.add(new StringField(key, value.asText(), Field.Store.YES));
        }
    }

    // index field without stemming but store original string value
    private void addNonStemmedField(Document doc, String key, String value, FieldType fieldType) {
        FieldType nonStemmedType = new FieldType(fieldType);
        nonStemmedType.setStored(true);

        // token stream to be indexed
        Analyzer nonStemmingAnalyzer = DefaultEnglishAnalyzer.newNonStemmingInstance(CharArraySet.EMPTY_SET);
        TokenStream stream = nonStemmingAnalyzer.tokenStream(null, new StringReader(value));
        Field field = new Field(key, value, nonStemmedType);
        field.setTokenStream(stream);
        doc.add(field);
        nonStemmingAnalyzer.close();
    }
}