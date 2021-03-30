package io.anserini.rerank.lib;

import java.io.IOException;

import io.anserini.util.IndexWrapper;
import io.anserini.util.GQuery;
import io.anserini.util.SearchHit;
import io.anserini.util.SearchHits;
import io.anserini.util.RocchioFeatureVector;
import io.anserini.util.Stopper;

public class RocchioReranker {

    private double alpha;
    private double beta;
    private double k1;
    private double b;

    private Stopper stoplist = new Stopper();

    /**
     * Default parameter values taken from:
     * https://nlp.stanford.edu/IR-book/html/htmledition/the-rocchio71-algorithm-1.html
     */
    public RocchioReranker() {
        this(1.0, 0.75);
    }

    public RocchioReranker(double alpha, double beta) {
        this(alpha, beta, 1.2, 0.75);
    }

    public RocchioReranker(double alpha, double beta, double k1, double b) {
        this.alpha = alpha;
        this.beta = beta;
        this.k1 = k1;
        this.b = b;
    }

    public void setStopper(Stopper stoplist) {
        this.stoplist = stoplist;
    }

    public void expandQuery(IndexWrapper index, GQuery query, int fbDocs, int fbTerms) throws IOException {

        SearchHits hits = index.runQuery(query, fbDocs);

        RocchioFeatureVector feedbackVec = new RocchioFeatureVector(stoplist);

        for (SearchHit hit: hits.hits()) {
            // Get the document tokens and add to the doc vector
            RocchioFeatureVector docVec = index.getDocVector(hit.getDocID(), stoplist);

            // Compute the BM25 weights and add to the feedbackVector
            computeBM25Weights(index, docVec, feedbackVec);
        }

        // Multiply the summed term vector by beta / |Dr|
        RocchioFeatureVector relDocTermVec = new RocchioFeatureVector(stoplist);
        for (String term : feedbackVec.getFeatures()) {
            relDocTermVec.addTerm(term, feedbackVec.getFeatureWeight(term) * beta / fbDocs);
        }

        // Create a query vector and scale by alpha
        RocchioFeatureVector origQueryVec = query.getRocchioFeatureVector();

        RocchioFeatureVector weightedQueryVec = new RocchioFeatureVector(stoplist);
        computeBM25Weights(index, origQueryVec, weightedQueryVec);

        RocchioFeatureVector queryTermVec = new RocchioFeatureVector(stoplist);
        for (String term : origQueryVec.getFeatures()) {
            queryTermVec.addTerm(term, weightedQueryVec.getFeatureWeight(term) * alpha);
        }

        // Combine query and feedback vectors
        for (String term : queryTermVec.getFeatures()) {
            relDocTermVec.addTerm(term, queryTermVec.getFeatureWeight(term));
        }

        // Get top terms
        relDocTermVec.clip(fbTerms);

        query.setRocchioFeatureVector(relDocTermVec);
    }

    private void computeBM25Weights(IndexWrapper index, RocchioFeatureVector docVec, RocchioFeatureVector summedTermVec) throws IOException {
        for (String term : docVec.getFeatures()) {
            double docCount = index.docCount();
            double docOccur = index.docFreq(term);
            double avgDocLen = index.docLengthAvg();

            double idf = Math.log( (docCount + 1) / (docOccur + 0.5) ); // following Indri
            double tf = docVec.getFeatureWeight(term);

            double weight = (idf * k1 * tf) / (tf + k1 * (1 - b + b * docVec.getLength() / avgDocLen));

            summedTermVec.addTerm(term, weight);
        }
    }
}

