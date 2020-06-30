package io.anserini.index.generator;

import io.anserini.collection.GRDCCollection;
import io.anserini.index.IndexArgs;
import org.apache.lucene.document.*;

public class GRDCGenerator extends DefaultLuceneDocumentGenerator<GRDCCollection.Document> {
    protected IndexArgs args;

    // constants for storing
    public enum GRDCFields {
        ID("id"), TITLE("title"), ABSTRACT("abstract"), ORGANISATION("organisation"),
        RESPONSIBLE_PARTY("responsible_party"), CATALOGUE("catalogue"), PUBLISH_TIME("publish_time"), URL("url"),
        COORDINATES("coordinates");

        public final String name;

        GRDCFields(String s) {
            name = s;
        }
    }

    public GRDCGenerator(IndexArgs args) {
        super(args);
        this.args = args;
    }

    public Document createDocument(GRDCCollection.Document doc) throws GeneratorException {
        Document document = super.createDocument(doc);

        document.add(new StoredField(GRDCFields.TITLE.name, doc.getTitle()));
        document.add(new StoredField(GRDCFields.ABSTRACT.name, doc.getAbstract()));
        document.add(new StringField(GRDCFields.ORGANISATION.name, doc.getOrganisation(), Field.Store.YES));
        document.add(new StringField(GRDCFields.CATALOGUE.name, doc.getCatalogue(), Field.Store.YES));
        document.add(new StringField(GRDCFields.PUBLISH_TIME.name, doc.getPublish_time(), Field.Store.YES));
        document.add(new StringField(GRDCFields.URL.name, doc.getUrl(), Field.Store.YES));

        // indexing the authors
        String[] responsibleParty = doc.getResponsibleParty();
        for (String author : responsibleParty) {
            document.add(new StringField(GRDCFields.RESPONSIBLE_PARTY.name, author, Field.Store.YES));
        }

        // indexing the coordinates
        document.add(new StringField(GRDCFields.COORDINATES.name, doc.getCoordinates(), Field.Store.YES));
        /*
         * Polygon indexing unused for now, but may be used later // indexing the
         * longitude and latitudes, each field is an indexable ShapeField.Triangle
         * object Field[] polygonField =
         * LatLonShape.createIndexableFields(Iso19115Field.COORDINATES.name, new
         * Polygon( doc.getLatitude(), doc.getLongitude() )); for(Field field:
         * polygonField) { document.add(field); }
         */
        return document;
    }
}
