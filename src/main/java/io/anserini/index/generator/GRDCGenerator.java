package io.anserini.index.generator;

import io.anserini.collection.GRDCCollection;
import io.anserini.index.IndexArgs;
import org.apache.lucene.document.*;

public class GRDCGenerator extends DefaultLuceneDocumentGenerator<GRDCCollection.Document> {
    protected IndexArgs args;

    // constants for storing
    public enum GRDCFields {
        REPORT_ID("report_id"), PROJECT_NUMBER("project_number"), REPORT_TITLE("report_title"),
        REGION_NAME("region_name"), CATEGORY_NAME("category_name"), RESEARCH_THEME_NAME("research_theme_name"),
        ORGANIZATION_NAME("organization_name"), COMMENCE_DATE("commence_date"), COMPLETE_DATE("complete_date"),
        STATE("state"), SUPERVISOR_NAME("supervisor_name"), REPORT_TYPE("report_type"), REPORT_STATUS("report_status"),
        PUBLISH_DATE("publish_date"), REPORT_SUMMARY("report_summary"), KEYWORDS("keywords"), PDF_URL("pdf_url"),
        WEB_URL("web_url"), HTML_CONTENT("html_content"), REPORT_ACHIEVEMENT("report_achievement"),
        REPORT_CONCLUSION("report_conclusion"), REPORT_OUTCOME("report_outcome"),
        REPORT_RECOMMENDATION("report_recommendation"), REPORT_DISCUSSION("report_discussion"),
        OTHER_RESEARCH("other_research"), IP_SUMMARY("ip_summary"), ADDITIONAL_INFORMATION("additional_information"),
        REPORT_FULL_TEXT_CONTENT("report_full_text_content"), ATTACHMENTS("attachments"),
        ATTACHMENT_URL("attachment_url"), ATTACHMENT_ID("attachment_id"), ATTACHMENT_NAME("attachment_name"),
        ATTACHMENT_SIZE("attachment_size"), ATTACHMENT_TYPE("attachment_type"),
        ATTACHMENT_BASE64_CONTENT("attachment_base64_content"),
        ATTACHMENT_FULL_TEXT_CONTENT("attachment_full_text_content"), RAW("raw");

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

        document.add(new StoredField(GRDCFields.REPORT_ID.name, doc.id()));

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
