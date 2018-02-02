package dk.thesis.sbsa.simplejob;

import com.google.gson.Gson;
import edu.stanford.nlp.pipeline.Annotation;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.time.LocalDateTime;

public class StreamingJob {

    static Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setLatencyTrackingInterval(2000);

        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        String inTopic = parameterTool.get("topic-in", "");
        String outTopic = parameterTool.get("topic-out", "");
        String kafkaserver = parameterTool.get("bootstrap.servers", "localhost");


        FlinkKafkaConsumer010<String> kafkaSource = new FlinkKafkaConsumer010<String>(
                inTopic,
                new SimpleStringSchema(),
                parameterTool.getProperties()
        );

        env.addSource(kafkaSource).map(
                new MapFunction<String, MessageObject>() {

                    @Override
                    public MessageObject map(String inputString) throws Exception {
                        String receivedAt = LocalDateTime.now().toString();

                        MessageObject msgObj = gson.fromJson(inputString, MessageObject.class);
                        msgObj.flinkClassifyRecievedAt = receivedAt;

                        return msgObj;
                    }

                }).name("Str->Pojo")
                .map(new RichMapFunction<MessageObject, MessageObject>() {

                    @Override
                    public MessageObject map(MessageObject value) throws Exception {

                        long start = System.nanoTime();

                        value.serializedByteArray = PayloadSerializer.decompress(value.serializedByteArray);
                        Annotation deserializedAnnotation = (Annotation) PayloadSerializer.deserialize(value.serializedByteArray);
                        Double sentiment = processor.getSentiment(deserializedAnnotation);
                        value.sentiment = String.valueOf(sentiment);
                        value.serializedByteArray = null; // No need for this anymore.

                        long end = System.nanoTime();
                        value.sentimentClassifyRuntime = (end - start);
                        return value;
                    }

                    private StanfordSentimentProcessor processor;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        processor = StanfordSentimentProcessor.create();
                    }

                }).name("Senti-Classify-Map")
                .map(new MapFunction<MessageObject, String>() {
                    @Override
                    public String map(MessageObject value) throws Exception {
                        value.flinkClassifyEnd = LocalDateTime.now().toString();

                        return gson.toJson(value);
                    }

                }).name("Pojo->Jsonstr").
                addSink(new FlinkKafkaProducer010<String>(
                        kafkaserver, outTopic, new SimpleStringSchema())
                );
        // execute program

        System.out.println();
        System.out.println();
        System.out.println(env.getExecutionPlan());
        System.out.println();
        System.out.println();
        env.execute("Flink Streaming Sentiment Analysis Classify");
    }
}
