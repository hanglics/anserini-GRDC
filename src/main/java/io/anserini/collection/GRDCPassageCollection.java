package io.anserini.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class GRDCPassageCollection extends DocumentCollection<GRDCPassageCollection.Document> {
    public GRDCPassageCollection(Path path) {
        this.path = path;
        this.allowedFileSuffix = new HashSet<>(Arrays.asList(".json"));
    }

    @Override
    public FileSegment<GRDCPassageCollection.Document> createFileSegment(Path p) throws IOException {
        return new Segment(p);
    }

    public static class Segment extends FileSegment<GRDCPassageCollection.Document> {
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
                bufferedRecord = new GRDCPassageCollection.Document(node);
                if (iterator.hasNext()) {
                    node = iterator.next();
                } else {
                    atEOF = true;
                }
            } else if (node.isArray()) {
                if (iter != null && iter.hasNext()) {
                    bufferedRecord = new GRDCPassageCollection.Document(node);
                } else {
                    throw new NoSuchElementException("Reached end of JsonNode iterator");
                }
            } else {
                throw new NoSuchElementException("Invalid JsonNode type");
            }
        }
    }

    public static class Document implements SourceDocument {
        protected String pid;
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
        protected String[] keywords;
        protected String pdf_url;
        protected String web_url;
        protected String passage;
        protected String raw;

        public Document(JsonNode json) {
            // extracting the fields from the GRDC json file
            this.raw = json.toString();

            this.pid = json.get("pid").asText();
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
            this.passage = json.get("passage").asText();


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
        }

        @Override
        public String id() {
            return pid;
        }

        @Override
        public String contents() {
            return passage;
        }

        @Override
        public String raw() {
            return raw;
        }

        @Override
        public boolean indexable() {
            return true;
        }

        public String getReportID() {
            return report_id;
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

        public String getPublishDate() {
            return publish_date;
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

        public String getPassage() {
            return passage;
        }

    }
}
