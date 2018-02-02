package dk.thesis.sbsa.simplejob;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.JSONOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.IOException;
import java.util.Properties;


public class StanfordSentimentProcessor {

    private StanfordCoreNLP tokenizer;

    public StanfordSentimentProcessor() {
    }

    public static StanfordSentimentProcessor create() {

        Properties tokenizerProps = new Properties();

        tokenizerProps.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP tokenizer = new StanfordCoreNLP(tokenizerProps);

        StanfordSentimentProcessor sentimentProcessor = new StanfordSentimentProcessor();
        sentimentProcessor.tokenizer = tokenizer;

        return sentimentProcessor;
    }

    public Annotation preprocess(String text) {
        return tokenizer.process(text);
    }

    public String annotationToJsonString(Annotation annotation) {
        try {
            return new JSONOutputter().print(annotation);
        } catch (IOException e) {
            return "";
        }
    }
}