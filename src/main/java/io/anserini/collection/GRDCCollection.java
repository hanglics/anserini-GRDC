package io.anserini.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class GRDCCollection extends DocumentCollection<GRDCCollection.Document> {
    public GRDCCollection(Path path) {
        this.path = path;
        this.allowedFileSuffix = new HashSet<>(Arrays.asList(".json"));
    }

    @Override
    public FileSegment<GRDCCollection.Document> createFileSegment(Path p) throws IOException {
        return new Segment(p);
    }

    public static class Segment extends FileSegment<GRDCCollection.Document> {
        private JsonNode node = null;
        private Iterator<JsonNode> iter = null;
        private MappingIterator<JsonNode> iterator;

        public Segment(Path path) throws IOException {
            super(path);
            bufferedReader = new BufferedReader(new FileReader(path.toString()));
            ObjectMapper mapper = new ObjectMapper();
            iterator = mapper.readerFor(JsonNode.class).readValues(bufferedReader);
            if (iterator.hasNext()) {
                node = iterator.next();
                if (node.isArray()) {
                    iter = node.elements();
                }
            }
        }

        @Override
        public void readNext() throws NoSuchElementException {
            if (node == null) {
                throw new NoSuchElementException("JsonNode is empty");
            } else if (node.isObject()) {
                bufferedRecord = new GRDCCollection.Document(node);
                if (iterator.hasNext()) {
                    node = iterator.next();
                } else {
                    atEOF = true;
                }
            } else if (node.isArray()) {
                if (iter != null && iter.hasNext()) {
                    bufferedRecord = new GRDCCollection.Document(node);
                } else {
                    throw new NoSuchElementException("Reached end of JsonNode iterator");
                }
            } else {
                throw new NoSuchElementException("Invalid JsonNode type");
            }
        }
    }

    public static class Attachment {
        public String report_id;
        public String attachment_url;
        public String attachment_id;
        public String attachment_name;
        public String attachment_size;
        public String attachment_type;
        public String attachment_base64_content;
        public String attachment_full_text_content;
    }

    public static class Document implements SourceDocument {
        protected String report_id;
        protected String project_number;
        protected String report_title;
        protected String region_name;
        protected String category_name;
        protected String research_theme_name;
        protected String organization_name;
        protected String commence_date;
        protected String complete_date;
        protected String state;
        protected String supervisor_name;
        protected String report_type;
        protected String report_status;
        protected String publish_date;
        protected String report_summary;
        protected String[] keywords;
        protected String pdf_url;
        protected String web_url;
        protected String html_content;
        protected String report_achievement;
        protected String report_conclusion;
        protected String report_outcome;
        protected String report_recommendation;
        protected String report_discussion;
        protected String other_research;
        protected String ip_summary;
        protected String additional_information;
        protected String report_full_text_content;
        protected Attachment[] attachments;
        protected String raw;

        public Document(JsonNode json) {
            // extracting the fields from the GRDC json file
            this.raw = json.toString();

            this.report_id = json.get("report_id").asText();
            this.project_number = json.get("project_number").asText();
            this.report_title = json.get("report_title").asText();
            this.region_name = json.get("region_name").asText();
            this.category_name = json.get("category_name").asText();
            this.research_theme_name = json.get("research_theme_name").asText();
            this.organization_name = json.get("organization_name").asText();
            this.commence_date = json.get("commence_date").asText();
            this.complete_date = json.get("complete_date").asText();
            this.state = json.get("state").asText();
            this.supervisor_name = json.get("supervisor_name").asText();
            this.report_type = json.get("report_type").asText();
            this.report_status = json.get("report_status").asText();
            this.publish_date = json.get("publish_date").asText();
            this.report_summary = json.get("report_summary").asText();
            this.pdf_url = json.get("pdf_url").asText();
            this.web_url = json.get("web_url").asText();
            this.html_content = json.get("html_content").asText();
            this.report_achievement = json.get("report_achievement").asText();
            this.report_conclusion = json.get("report_conclusion").asText();
            this.report_outcome = json.get("report_outcome").asText();
            this.report_recommendation = json.get("report_recommendation").asText();
            this.report_discussion = json.get("report_discussion").asText();
            this.other_research = json.get("other_research").asText();
            this.ip_summary = json.get("ip_summary").asText();
            this.additional_information = json.get("additional_information").asText();
            this.report_full_text_content = json.get("report_full_text_content").asText();

            // get all keywords as JsonNode
            JsonNode keywords_node = json.get("keywords");

            // extracting all keywords from keywords field
            int number_of_keywords = keywords_node.size();
            this.keywords = new String[number_of_keywords];

            // check if there are keywords, if not, assign empty array
            if (number_of_keywords > 0) {
                for (int i = 0; i < number_of_keywords; i++) {
                    this.keywords[i] = keywords_node.get(i).asText();
                }
            }

            // get all attachments as JsonNode
            JsonNode attachments_node = json.get("attachments");

            // extracting all attachments from the attachments field
            int number_of_attachments = attachments_node.size();
            this.attachments = new Attachment[number_of_attachments];

            // check if there are attachments, if not, assign empty array
            if (number_of_attachments > 0) {
                // extracting individual attachment from all attachments
                for (int i = 0; i < number_of_attachments; i++) {
                    this.attachments[i].attachment_base64_content = attachments_node.get(i)
                            .get("attachment_base64_content").asText();
                    this.attachments[i].attachment_full_text_content = attachments_node.get(i)
                            .get("attachment_full_text_content").asText();
                    this.attachments[i].attachment_id = attachments_node.get(i).get("attachment_id").asText();
                    this.attachments[i].attachment_name = attachments_node.get(i).get("attachment_name").asText();
                    this.attachments[i].attachment_size = attachments_node.get(i).get("attachment_size").asText();
                    this.attachments[i].attachment_type = attachments_node.get(i).get("attachment_type").asText();
                    this.attachments[i].attachment_url = attachments_node.get(i).get("attachment_url").asText();
                    this.attachments[i].report_id = attachments_node.get(i).get("report_id").asText();
                }
            }
        }

        @Override
        public String id() {
            return report_id;
        }

        @Override
        public String contents() {
            return report_full_text_content;
        }

        @Override
        public String raw() {
            return raw;
        }

        @Override
        public boolean indexable() {
            return true;
        }

        public String getProjectNumber() {
            return project_number;
        }

        public String getReportTitle() {
            return report_title;
        }

        public String getRegionName() {
            return region_name;
        }

        public String getCategoryName() {
            return category_name;
        }

        public String getResearchThemeName() {
            return research_theme_name;
        }

        public String getOrganizationName() {
            return organization_name;
        }

        public String getCommenceDate() {
            return commence_date;
        }

        public String getCompleteDate() {
            return complete_date;
        }

        public String getState() {
            return state;
        }

        public String getSupervisorName() {
            return supervisor_name;
        }

        public String getReportType() {
            return report_type;
        }

        public String getReportStatus() {
            return report_status;
        }

        public String getPublishDate() {
            return publish_date;
        }

        public String getReportSummary() {
            return report_summary;
        }

        public String[] getKeywords() {
            return keywords;
        }

        public String getPDFURL() {
            return pdf_url;
        }

        public String getWebURL() {
            return web_url;
        }

        public String getHTMLContent() {
            return html_content;
        }

        public String getReportAchievement() {
            return report_achievement;
        }

        public String getReportConclusion() {
            return report_conclusion;
        }

        public String getReportOutcome() {
            return report_outcome;
        }

        public String getReportRecommendation() {
            return report_recommendation;
        }

        public String getReportDiscussion() {
            return report_discussion;
        }

        public String getOtherResearch() {
            return other_research;
        }

        public String getIPSummary() {
            return ip_summary;
        }

        public String getAdditionalInformation() {
            return additional_information;
        }

        public Attachment[] getAttachments() {
            return attachments;
        }

    }
}
