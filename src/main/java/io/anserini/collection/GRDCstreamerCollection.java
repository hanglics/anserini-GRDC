package io.anserini.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A document collection for the Trialstreamer dataset modelled after CORD-19.
 */
public class GRDCstreamerCollection extends DocumentCollection<GRDCstreamerCollection.Document> {
    private static final Logger LOG = LogManager.getLogger(GRDCstreamerCollection.class);

    public GRDCstreamerCollection(Path path) {
        this.path = path;
        this.allowedFileSuffix = Set.of(".csv");
    }

    @Override
    public FileSegment<GRDCstreamerCollection.Document> createFileSegment(Path p) throws IOException {
        return new Segment(p);
    }

    /**
     * A file containing a single CSV document.
     */
    public class Segment extends FileSegment<GRDCstreamerCollection.Document> {
        CSVParser csvParser = null;
        private CSVRecord record = null;
        private Iterator<CSVRecord> iterator = null; // iterator for CSV records

        public Segment(Path path) throws IOException {
            super(path);
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toString())));

            csvParser = new CSVParser(bufferedReader,
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

            iterator = csvParser.iterator();
            if (iterator.hasNext()) {
                record = iterator.next();
            }
        }

        @Override
        public void readNext() throws NoSuchElementException {
            if (record == null) {
                throw new NoSuchElementException("Record is empty");
            } else {
                bufferedRecord = new GRDCstreamerCollection.Document(record);
                if (iterator.hasNext()) { // if CSV contains more lines, we parse the next record
                    record = iterator.next();
                } else {
                    atEOF = true; // there is no more JSON object in the bufferedReader
                }
            }
        }

        @Override
        public void close() {
            super.close();
            if (csvParser != null) {
                try {
                    csvParser.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    /**
     * A document in a GRDC collection.
     */
    public class Document extends GRDCBaseDocument {
        private JsonNode facets;

        public Document(CSVRecord record) {
            id = record.get("cord_uid");
            content = record.get("title").replace("\n", " ");
            content += record.get("abstract").isEmpty() ? "" : "\n" + record.get("abstract");
            this.record = record;

            String fullTextJson = getFullTextJson(GRDCstreamerCollection.this.path.toString());
            if (fullTextJson != null) {
                raw = fullTextJson;
                StringReader fullTextReader = new StringReader(fullTextJson);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode recordJsonNode = mapper.readerFor(JsonNode.class).readTree(fullTextReader);
                    facets = recordJsonNode.get("facets");
                } catch (IOException e) {
                    LOG.error("Could not read JSON string");
                }
            } else {
                String recordJson = getRecordJson();
                raw = recordJson == null ? "" : recordJson;
            }
        }

        public JsonNode facets() {
            return facets;
        }
    }
}
