package io.anserini.index.generator;

import io.anserini.collection.GRDCCollection;
import io.anserini.collection.GRDCCollection.Attachment;
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

        document.add(new StringField(GRDCFields.REPORT_ID.name, doc.id(), Field.Store.YES));
        document.add(new StringField(GRDCFields.PROJECT_NUMBER.name, doc.getProjectNumber(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_TITLE.name, doc.getReportTitle(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REGION_NAME.name, doc.getRegionName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.CATEGORY_NAME.name, doc.getCategoryName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.RESEARCH_THEME_NAME.name, doc.getResearchThemeName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.ORGANIZATION_NAME.name, doc.getOrganizationName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.COMMENCE_DATE.name, doc.getCommenceDate(), Field.Store.YES));
        document.add(new StringField(GRDCFields.COMPLETE_DATE.name, doc.getCompleteDate(), Field.Store.YES));
        document.add(new StringField(GRDCFields.STATE.name, doc.getState(), Field.Store.YES));
        document.add(new StringField(GRDCFields.SUPERVISOR_NAME.name, doc.getSupervisorName(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_TYPE.name, doc.getReportType(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_STATUS.name, doc.getReportStatus(), Field.Store.YES));
        document.add(new StringField(GRDCFields.PUBLISH_DATE.name, doc.getPublishDate(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_SUMMARY.name, doc.getReportSummary(), Field.Store.YES));
        document.add(new StringField(GRDCFields.PDF_URL.name, doc.getPDFURL(), Field.Store.YES));
        document.add(new StringField(GRDCFields.WEB_URL.name, doc.getWebURL(), Field.Store.YES));
        document.add(new StringField(GRDCFields.HTML_CONTENT.name, doc.getHTMLContent(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_ACHIEVEMENT.name, doc.getReportAchievement(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_CONCLUSION.name, doc.getReportConclusion(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_OUTCOME.name, doc.getReportOutcome(), Field.Store.YES));
        document.add(
                new StringField(GRDCFields.REPORT_RECOMMENDATION.name, doc.getReportRecommendation(), Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_DISCUSSION.name, doc.getReportDiscussion(), Field.Store.YES));
        document.add(new StringField(GRDCFields.OTHER_RESEARCH.name, doc.getOtherResearch(), Field.Store.YES));
        document.add(new StringField(GRDCFields.IP_SUMMARY.name, doc.getIPSummary(), Field.Store.YES));
        document.add(new StringField(GRDCFields.ADDITIONAL_INFORMATION.name, doc.getAdditionalInformation(),
                Field.Store.YES));
        document.add(new StringField(GRDCFields.REPORT_FULL_TEXT_CONTENT.name, doc.contents(), Field.Store.YES));
        document.add(new StringField(GRDCFields.RAW.name, doc.raw(), Field.Store.YES));

        // examples of StoredField and StringField
        // document.add(new StoredField(GRDCFields.TITLE.name, doc.getTitle()));
        // document.add(new StringField(GRDCFields.ORGANISATION.name,
        // doc.getOrganisation(), Field.Store.YES));

        // indexing the keywords
        String[] keywords = doc.getKeywords();
        for (String keyword : keywords) {
            document.add(new StringField(GRDCFields.KEYWORDS.name, keyword, Field.Store.YES));
        }

        // indexing the attachments
        Attachment[] attachments = doc.getAttachments();
        for (Attachment attach : attachments) {
            document.add(new StringField(GRDCFields.REPORT_ID.name, attach.report_id, Field.Store.YES));
            document.add(new StringField(GRDCFields.ATTACHMENT_URL.name, attach.attachment_url, Field.Store.YES));
            document.add(new StringField(GRDCFields.ATTACHMENT_ID.name, attach.attachment_id, Field.Store.YES));
            document.add(new StringField(GRDCFields.ATTACHMENT_NAME.name, attach.attachment_name, Field.Store.YES));
            document.add(new StringField(GRDCFields.ATTACHMENT_SIZE.name, attach.attachment_size, Field.Store.YES));
            document.add(new StringField(GRDCFields.ATTACHMENT_TYPE.name, attach.attachment_name, Field.Store.YES));
            document.add(new StringField(GRDCFields.ATTACHMENT_BASE64_CONTENT.name, attach.attachment_base64_content,
                    Field.Store.YES));
            document.add(new StringField(GRDCFields.ATTACHMENT_FULL_TEXT_CONTENT.name,
                    attach.attachment_full_text_content, Field.Store.YES));
        }

        return document;
    }
}
