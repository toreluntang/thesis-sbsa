package dk.thesis.sbsa.simplejob;

import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Properties;


public class StanfordSentimentProcessor {

    private StanfordCoreNLP pipeline;

    public StanfordSentimentProcessor() {
    }


    public static StanfordSentimentProcessor create() {

        Properties pipelineProps = new Properties();

        pipelineProps.setProperty("enforceRequirements", "false");
        pipelineProps.setProperty("annotators", "parse, sentiment");
        pipelineProps.setProperty("parse.binaryTrees", "true");
        pipelineProps.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
//        pipelineProps.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        pipelineProps.setProperty("tokenize.language", "en");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(pipelineProps);

        StanfordSentimentProcessor sentimentProcessor = new StanfordSentimentProcessor();
        sentimentProcessor.pipeline = pipeline;

        return sentimentProcessor;
    }

    public Double getSentiment(Annotation annotation) {

        try {

            pipeline.annotate(annotation);

//            JSONOutputter.jsonPrint(annotation);
            double totalSentiment = 0.0;
            int numberOfSentences = 0;

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

                String output = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);

                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);

                totalSentiment += sentiment;
                numberOfSentences++;
            }

            totalSentiment = totalSentiment / numberOfSentences;
            return totalSentiment;

        } catch (Exception e) {
            System.out.println("Binarized sentences not built by parser exception.");
            System.out.println("Text pass as argument was: " + annotation.toString());
            System.out.println("pipeline obj: " + pipeline.toString());
            return -999.0;
        }
    }
}