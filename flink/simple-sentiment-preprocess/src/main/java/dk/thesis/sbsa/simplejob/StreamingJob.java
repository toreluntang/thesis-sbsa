package dk.thesis.sbsa.simplejob;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StreamingJob {

    static Set<String> stopwords = new HashSet(Arrays.asList("sometime", "been", "mostly", "hasnt", "about", "your", "anywhere", "somewhere", "wherein", "without", "via", "these", "would", "above", "fire", "let", "because", "ten", "they", "you", "afterwards", "thus", "meanwhile", "myself", "bill", "herein", "them", "then", "am", "yourselves", "an", "whose", "each", "former", "something", "mill", "as", "himself", "at", "re", "thereby", "twelve", "must", "eleven", "except", "detail", "sincere", "much", "nevertheless", "be", "another", "least", "two", "anyway", "seem", "how", "into", "see", "found", "same", "are", "does", "by", "whom", "where", "after", "dear", "so", "mine", "a", "sixty", "though", "namely", "one", "i", "co", "many", "the", "call", "such", "to", "describe", "under", "yours", "did", "but", "through", "de", "nine", "becoming", "sometimes", "had", "cant", "do", "got", "down", "empty", "either", "whenever", "besides", "yourself", "has", "up", "five", "us", "those", "tis", "beforehand", "which", "seeming", "eg", "might", "this", "its", "thereafter", "often", "onto", "whatever", "she", "take", "once", "everywhere", "name", "therefore", "however", "next", "some", "rather", "for", "show", "back", "we", "anything", "nor", "not", "nowhere", "perhaps", "now", "themselves", "throughout", "wants", "hence", "every", "just", "forty", "over", "six", "thence", "again", "was", "go", "yet", "indeed", "with", "what", "although", "there", "fify", "well", "he", "very", "therein", "whole", "during", "none", "when", "beyond", "three", "put", "her", "whoever", "else", "four", "beside", "whereas", "ie", "nobody", "per", "if", "between", "likely", "give", "still", "in", "made", "anyhow", "is", "it", "being", "ever", "itself", "toward", "system", "hereupon", "even", "among", "anyone", "whereby", "other", "hundred", "whereupon", "our", "ourselves", "eight", "out", "across", "couldnt", "top", "too", "moreover", "get", "have", "twenty", "wherever", "side", "may", "seemed", "within", "could", "more", "off", "able", "cannot", "hereby", "whereafter", "first", "thru", "con", "almost", "before", "own", "several", "amoungst", "while", "upon", "him", "latterly", "that", "amongst", "his", "etc", "find", "whether", "than", "me", "only", "should", "few", "from", "all", "always", "otherwise", "whither", "like", "already", "below", "everyone", "thickv", "bottom", "towards", "my", "fill", "done", "becomes", "both", "most", "were", "keep", "herself", "seems", "thereupon", "since", "who", "here", "no", "became", "behind", "twas", "part", "their", "why", "elsewhere", "around", "hers", "can", "alone", "along", "and", "of", "somehow", "said", "says", "ltd", "on", "inc", "fifteen", "amount", "or", "will", "whence", "hereafter", "also", "say", "enough", "any", "someone", "third", "due", "neither", "latter", "until", "front", "further", "formerly"));
    static JsonParser jsonParser = new JsonParser();
    static Gson gson = new Gson();


    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setLatencyTrackingInterval(2000);

        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        String inTopic = parameterTool.get("topic-in", "");
        String outTopic = parameterTool.get("topic-out", "");
        String kafkaserver = parameterTool.get("bootstrap.servers", "localhost");
//        String groupid = parameterTool.get("group.id");

        FlinkKafkaConsumer010<String> kafkaSource = new FlinkKafkaConsumer010<String>(
                inTopic,
                new SimpleStringSchema(),
                parameterTool.getProperties()
        );
//        kafkaSource.setStartFromEarliest();

        env.addSource(kafkaSource).rebalance().map(
                new MapFunction<String, MessageObject>() {

                    @Override
                    public MessageObject map(String inputString) throws Exception {

                        String receivedAt = LocalDateTime.now().toString();

                        JsonObject value = jsonParser.parse(inputString).getAsJsonObject();

                        MessageObject msgObj = new MessageObject();
                        msgObj.flinkPreprocesRecievedAt = receivedAt;
                        msgObj.love = getReactionCount("LOVE", value);
                        msgObj.haha = getReactionCount("HAHA", value);
                        msgObj.wow = getReactionCount("WOW", value);
                        msgObj.sad = getReactionCount("SAD", value);
                        msgObj.angry = getReactionCount("ANGRY", value);

                        if (null != value) {

                            if (value.has("message") && !value.get("message").isJsonNull()) {
                                msgObj.message = value.get("message").getAsString();
                            }

                            if (value.has("name") && !value.get("name").isJsonNull()) {
                                msgObj.message += ". " + value.get("name").getAsString() + ". ";
                            }

                            if (value.has("description") && !value.get("description").isJsonNull()) {
                                msgObj.message += ". " + value.get("description").getAsString() + ". ";
                            }

                            if (value.has("id") && !value.get("id").isJsonNull()) {
                                msgObj.id = value.get("id").getAsString();
                            }

                            if (value.has("username") && !value.get("username").isJsonNull()) {
                                msgObj.username = value.get("username").getAsString();
                            }

                            if (value.has("caption") && !value.get("caption").isJsonNull()) {
                                msgObj.caption = value.get("caption").getAsString();
                            }

                            if (value.has("enter_system_time") && !value.get("enter_system_time").isJsonNull()) {
                                msgObj.kafkaCreatedTime = value.get("enter_system_time").getAsString();
                            }
                        }

                        return msgObj;
                    }

                    private Integer getReactionCount(String reactionLabel, JsonObject json) {
                        Integer totalCount = 0;
                        if (json.has(reactionLabel) && !json.get(reactionLabel).isJsonNull()) {

                            JsonObject reactionJson = json.get(reactionLabel).getAsJsonObject();
                            if (reactionJson.has("summary") && !reactionJson.get("summary").isJsonNull()) {
                                JsonObject summary = reactionJson.get("summary").getAsJsonObject();

                                if (summary.has("total_count") && !summary.get("total_count").isJsonNull()) {
                                    if (summary.get("total_count") instanceof JsonPrimitive)
                                        totalCount = summary.get("total_count").getAsInt();
                                }
                            }
                        }
                        return totalCount;
                    }

                }).name("Str->Pojo")
                .map(new MapFunction<MessageObject, MessageObject>() {

                    @Override
                    public MessageObject map(MessageObject value) throws Exception {
                        String msg = value.message;
//                        msg = removeUrls(msg);
                        msg = removeBadSymbols(msg);
                        msg = removeStopWords(msg);
                        msg = msg.toLowerCase(); // Is this actually wrong to do
                        value.kerasPreprocessed = msg;

                        return value;
                    }
                }).name("Simple-Preprocess")
                .map(new RichMapFunction<MessageObject, MessageObject>() {

                    private StanfordSentimentProcessor processor;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        processor = StanfordSentimentProcessor.create();
                    }

                    @Override
                    public MessageObject map(MessageObject value) throws Exception {
                        long start = System.nanoTime();

                        Annotation preprocessed = processor.preprocess(value.kerasPreprocessed);
//                        value.serializedByteArray = PayloadSerializer.serialize(preprocessed);
//                        value.serializedByteArray = PayloadSerializer.compress(value.serializedByteArray);

                        value.kerasPreprocessed = preprocessed.toString();

                        long end = System.nanoTime();
                        value.sentimentPreprocessRuntime = (end - start);

                        return value;
                    }

                }).name("Senti-Preprocess-Map")
                .map(new MapFunction<MessageObject, String>() {
                    @Override
                    public String map(MessageObject value) throws Exception {
                        value.flinkPreprocesEnd = LocalDateTime.now().toString();
                        return gson.toJson(value);
                    }
                }).name("pojo->jsonstr").
                addSink(new FlinkKafkaProducer010<String>(
                        kafkaserver, outTopic, new SimpleStringSchema())
                );
        // execute program

        System.out.println();
        System.out.println();
        System.out.println(env.getExecutionPlan());
        System.out.println();
        System.out.println();

        env.execute("Flink Streaming Sentiment Analysis Preprocess");
    }


    private static String removeBadSymbols(String body) {
        return body.replaceAll("[#@~^=<>&\\_/\n]", "");
    }

    private static String removeStopWords(String msg) {

        StringBuilder stringBuilder = new StringBuilder();
        String[] words = msg.split(" ");

        for (String word : words) {
            if (!stopwords.contains(word)) {

                stringBuilder.append(word);
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    private static String removeUrls(String msg) {
        return msg.replaceAll("(?i)(?:https?|ftp):\\/\\/[\\n\\S]+", "");
    }

}
