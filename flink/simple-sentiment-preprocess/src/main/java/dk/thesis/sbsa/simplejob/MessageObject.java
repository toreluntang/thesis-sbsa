package dk.thesis.sbsa.simplejob;

import java.io.Serializable;

public class MessageObject implements Serializable {



    public int love;
    public int haha;
    public int wow;
    public int sad;
    public int angry;

    public String kafkaCreatedTime;
    public String flinkPreprocesRecievedAt;
    public String flinkClassifyRecievedAt;
    public String flinkPreprocesEnd;
    public String flinkClassifyEnd;
    public String message = "";
    public String id;
    public String username;
    public String description;
    public String caption;
    public String sentiment;
    public long   sentimentPreprocessRuntime;
    public long   sentimentClassifyRuntime;
    public String kerasPreprocessed;
    public byte[] serializedByteArray;

}
