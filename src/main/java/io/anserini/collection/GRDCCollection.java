package io.anserini.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
        public String[] attachment_full_text_content;

        public Attachment() {
            report_id = null;
            attachment_url = null;
            attachment_id = null;
            attachment_name = null;
            attachment_size = null;
            attachment_type = null;
            attachment_full_text_content = new String[0];
        }
    }

    public static class Document implements SourceDocument {
        protected String report_id;
        protected String project_number;
        protected String report_title;
        protected String region_name;
        protected String category_name;
        protected String research_theme_name;
        protected String organisation_name;
        protected String commence_date;
        protected String complete_date;
        protected String state;
        protected String supervisor_name;
        protected String report_type;
        protected String report_status;
        protected String publish_date;
        protected String[] report_summary;
        protected String[] keywords;
        protected String pdf_url;
        protected String web_url;
        protected String[] html_content;
        protected String[] report_achievement;
        protected String[] report_conclusion;
        protected String[] report_outcome;
        protected String[] report_recommendation;
        protected String[] report_discussion;
        protected String[] other_research;
        protected String ip_summary;
        protected String[] additional_information;
        protected String[] report_full_text_content;
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
            this.organisation_name = json.get("organisation_name").asText();
            this.commence_date = json.get("commence_date").asText();
            this.complete_date = json.get("complete_date").asText();
            this.state = json.get("state").asText();
            this.supervisor_name = json.get("supervisor_name").asText();
            this.report_type = json.get("report_type").asText();
            this.report_status = json.get("report_status").asText();
            this.publish_date = json.get("publish_date").asText();
            this.pdf_url = json.get("pdf_url").asText();
            this.web_url = json.get("web_url").asText();
            this.ip_summary = json.get("ip_summary").asText();

            JsonNode additional_information_node = json.get("additional_information");

            int number_of_additional_information = additional_information_node.size();
            this.additional_information = new String[number_of_additional_information];

            if (number_of_additional_information > 0) {
                for (int i = 0; i < number_of_additional_information; i++) {
                    this.additional_information[i] = additional_information_node.get(i).asText();
                }
            }

            JsonNode report_summary_node = json.get("report_summary");

            int number_of_report_summary = report_summary_node.size();
            this.report_summary = new String[number_of_report_summary];

            if (number_of_report_summary > 0) {
                for (int i = 0; i < number_of_report_summary; i++) {
                    this.report_summary[i] = report_summary_node.get(i).asText();
                }
            }

            JsonNode report_achievement_node = json.get("report_achievement");

            int number_of_achievement = report_achievement_node.size();
            this.report_achievement = new String[number_of_achievement];

            if (number_of_achievement > 0) {
                for (int i = 0; i < number_of_achievement; i++) {
                    this.report_achievement[i] = report_achievement_node.get(i).asText();
                }
            }

            JsonNode report_conclusion_node = json.get("report_conclusion");

            int number_of_conclusion = report_conclusion_node.size();
            this.report_conclusion = new String[number_of_conclusion];

            if (number_of_conclusion > 0) {
                for (int i = 0; i < number_of_conclusion; i++) {
                    this.report_conclusion[i] = report_conclusion_node.get(i).asText();
                }
            }

            JsonNode report_outcome_node = json.get("report_outcome");

            int number_of_outcome = report_outcome_node.size();
            this.report_outcome = new String[number_of_outcome];

            if (number_of_outcome > 0) {
                for (int i = 0; i < number_of_outcome; i++) {
                    this.report_outcome[i] = report_outcome_node.get(i).asText();
                }
            }

            JsonNode report_rec_node = json.get("report_recommendation");

            int number_of_rec = report_rec_node.size();
            this.report_recommendation = new String[number_of_rec];

            if (number_of_rec > 0) {
                for (int i = 0; i < number_of_rec; i++) {
                    this.report_recommendation[i] = report_rec_node.get(i).asText();
                }
            }

            JsonNode report_discussion_node = json.get("report_discussion");

            int number_of_discussion = report_discussion_node.size();
            this.report_discussion = new String[number_of_discussion];

            if (number_of_discussion > 0) {
                for (int i = 0; i < number_of_discussion; i++) {
                    this.report_discussion[i] = report_discussion_node.get(i).asText();
                }
            }

            JsonNode report_or_node = json.get("other_research");

            int number_of_or = report_or_node.size();
            this.other_research = new String[number_of_or];

            if (number_of_or > 0) {
                for (int i = 0; i < number_of_or; i++) {
                    this.other_research[i] = report_or_node.get(i).asText();
                }
            }

            // get all html contents as JsonNode
            JsonNode html_content_node = json.get("html_content");

            // extracting all html contents
            int number_of_html_contents = html_content_node.size();
            this.html_content = new String[number_of_html_contents];

            // check if there are contents
            if (number_of_html_contents > 0) {
                for (int k = 0; k < number_of_html_contents; k++) {
                    this.html_content[k] = html_content_node.get(k).asText();
                }
            }

            // get all full contents as JsonNode
            JsonNode full_content_node = json.get("report_full_text_content");

            // extracting all full contents
            int number_of_full_contents = full_content_node.size();
            this.report_full_text_content = new String[number_of_full_contents];

            // check if there are contents
            if (number_of_full_contents > 0) {
                for (int i = 0; i < number_of_full_contents; i++) {
                    this.report_full_text_content[i] = full_content_node.get(i).asText();
                }
            }

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

            // check if there are attachments, if not, assign empty array
            if (number_of_attachments > 0) {
                this.attachments = new Attachment[number_of_attachments];
                for (int l = 0; l < number_of_attachments; l++) {
                    this.attachments[l] = new Attachment();
                }
                // extracting individual attachment from all attachments
                for (int k = 0; k < number_of_attachments; k++) {
                    JsonNode attachment_content_node = attachments_node.get(k).get("attachment_full_text_content");

                    int number_of_attachment_contents = attachment_content_node.size();
                    String[] attachment_full_text_content = new String[number_of_attachment_contents];

                    for (int n = 0; n < number_of_attachment_contents; n++) {
                        attachment_full_text_content[n] = "";
                    }

                    if (number_of_attachment_contents > 0) {
                        for (int j = 0; j < number_of_attachment_contents; j++) {
                            attachment_full_text_content[j] = attachment_content_node.get(j).asText();
                        }
                    }

                    this.attachments[k].attachment_full_text_content = attachment_full_text_content;
                    this.attachments[k].attachment_id = attachments_node.get(k).get("attachment_id").asText();
                    this.attachments[k].attachment_name = attachments_node.get(k).get("attachment_name").asText();
                    this.attachments[k].attachment_size = attachments_node.get(k).get("attachment_size").asText();
                    this.attachments[k].attachment_type = attachments_node.get(k).get("attachment_type").asText();
                    this.attachments[k].attachment_url = attachments_node.get(k).get("attachment_url").asText();
                    this.attachments[k].report_id = attachments_node.get(k).get("report_id").asText();
                }
            } else {
                this.attachments = new Attachment[0];
            }
        }

        @Override
        public String id() {
            return report_id;
        }

        @Override
        public String contents() {
            return String.join(". ", report_full_text_content);
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

        public String getOrganisationName() {
            return organisation_name;
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

        public String[] getReportFullContent() {
            return report_full_text_content;
        }

        public String getPublishDate() {
            return publish_date;
        }

        public String[] getReportSummary() {
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

        public String[] getHTMLContent() {
            return html_content;
        }

        public String[] getReportAchievement() {
            return report_achievement;
        }

        public String[] getReportConclusion() {
            return report_conclusion;
        }

        public String[] getReportOutcome() {
            return report_outcome;
        }

        public String[] getReportRecommendation() {
            return report_recommendation;
        }

        public String[] getReportDiscussion() {
            return report_discussion;
        }

        public String[] getOtherResearch() {
            return other_research;
        }

        public String getIPSummary() {
            return ip_summary;
        }

        public String[] getAdditionalInformation() {
            return additional_information;
        }

        public Attachment[] getAttachments() {
            return attachments;
        }

    }
}
