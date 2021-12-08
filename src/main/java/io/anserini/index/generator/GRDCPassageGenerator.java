package io.anserini.index.generator;

import io.anserini.collection.GRDCPassageCollection;
import io.anserini.index.IndexArgs;
import org.apache.lucene.document.*;

public class GRDCPassageGenerator extends DefaultLuceneDocumentGenerator<GRDCPassageCollection.Document> {
    protected IndexArgs args;

    // constants for storing
    public enum GRDCFields {
        REPORT_ID("report_id"), 
		TYPE("type"), 
		PROJECT_NUMBER("project_number"), 
		REPORT_TITLE("report_title"),
        REGION_NAME("region_name"), 
		CATEGORY_NAME("category_name"), 
		RESEARCH_THEME_NAME("research_theme_name"),
        ORGANISATION_NAME("organisation_name"), 
        COMPLETE_DATE("complete_date"),
        SUPERVISOR_NAME("supervisor_name"), 
		PUBLISH_DATE("publish_date"), 
		REPORT_PATH("report_path"), 
		KEYWORDS("keywords"), 
		PDF_URL("pdf_url"),
        WEB_URL("web_url"), 
		SUMMARY("summary"), 
		TEXT("text");

        public final String name;

        GRDCFields(String s) {
            name = s;
        }
    }

    public GRDCPassageGenerator(IndexArgs args) {
        super(args);
        this.args = args;
    }

    public Document createDocument(GRDCPassageCollection.Document doc) throws GeneratorException {
        Document document = super.createDocument(doc);

        document.add(new StringField(GRDCFields.REPORT_ID.name, doc.getReportID(), Field.Store.YES));
        document.add(new StringField(GRDCFields.TYPE.name, doc.getType(), Field.Store.YES));
        document.add(new StringField(GRDCFields.PROJECT_NUMBER.name, doc.getProjectNumber(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_TITLE.name, doc.getReportTitle(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REGION_NAME.name, doc.getRegionName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.CATEGORY_NAME.name, doc.getCategoryName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.RESEARCH_THEME_NAME.name, doc.getResearchThemeName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.ORGANISATION_NAME.name, doc.getOrganisationName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.COMPLETE_DATE.name, doc.getCompleteDate(), Field.Store.YES));
        document.add(new StringField(GRDCFields.SUPERVISOR_NAME.name, doc.getSupervisorName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.PUBLISH_DATE.name, doc.getPublishDate(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_PATH.name, doc.getReportPath(), Field.Store.YES));
        document.add(new StringField(GRDCFields.PDF_URL.name, doc.getPDFURL(), Field.Store.YES));
        document.add(new StringField(GRDCFields.WEB_URL.name, doc.getWebURL(), Field.Store.YES));
        document.add(new StringField(GRDCFields.SUMMARY.name, doc.getSummary(), Field.Store.YES));
		document.add(new StringField(GRDCFields.TEXT.name, doc.getText(), Field.Store.YES));

        // indexing the keywords
        String[] keywords = doc.getKeywords();
        for (String keyword : keywords) {
            document.add(new StringField(GRDCFields.KEYWORDS.name, keyword, Field.Store.YES));
        }

        return document;
    }
}
