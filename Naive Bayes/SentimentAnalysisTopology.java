package com.zdatainc.rts.storm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

//import cmu.arktweetnlp.POSTagger;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;
import storm.kafka.StringScheme;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;

public class SentimentAnalysisTopology  implements Serializable
{
    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private static final String KAFKA_TOPIC =
        Properties.getString("rts.storm.kafka_topic");
    static File file = new File("/home/cloudera/group3/shen_testing.csv");
    static WekaTutorial wekaTutorial;

    
    
    public static void main(String[] args) throws Exception
    {
    	
    	
    	
    	// wekaTutorial = new WekaTutorial();
         
        // wekaTutorial.trainClassifier("/home/user/training.csv");
         //wekaTutorial.testClassifier("/home/user/testing.csv");
        
     //    MachineLearningBolt tfb = new MachineLearningBolt();
      //   tfb.getWekaObj(wekaTutorial);
         
         CSVBolt cb = new CSVBolt();
         
        // cb.writeCSVtitle();
         
        BasicConfigurator.configure();

        if (args != null && args.length > 0)
        {
            StormSubmitter.submitTopology(
                args[0],
                createConfig(false),
                createTopology());
        }
        else
        {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(
                "sentiment-analysis",
                createConfig(true),
                createTopology());
            Thread.sleep(60000);
            cluster.shutdown();
        }
    }

    private static StormTopology createTopology()
    {
        SpoutConfig kafkaConf = new SpoutConfig(
            new ZkHosts(Properties.getString("rts.storm.zkhosts")),
            KAFKA_TOPIC,
            "/kafka",
            "KafkaSpout");
        kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        TopologyBuilder topology = new TopologyBuilder();

        topology.setSpout("kafka_spout", new KafkaSpout(kafkaConf), 4);
        
        topology.setBolt("Csv_bolt", new CSVBolt(), 4)
        .shuffleGrouping("kafka_spout");
        
        topology.setBolt("weka_bolt", new WekaTutorial(), 4)
        .shuffleGrouping("Csv_bolt");

        

     /*   topology.setBolt("text_filter", new TextFilterBolt(), 4)
                .shuffleGrouping("twitter_filter");

        topology.setBolt("stemming", new StemmingBolt(), 4)
                .shuffleGrouping("text_filter");

        topology.setBolt("positive", new PositiveSentimentBolt(), 4)
                .shuffleGrouping("stemming");
        topology.setBolt("negative", new NegativeSentimentBolt(), 4)
                .shuffleGrouping("stemming");

        topology.setBolt("join", new JoinSentimentsBolt(), 4)
                .fieldsGrouping("positive", new Fields("tweet_id"))
                .fieldsGrouping("negative", new Fields("tweet_id"));

        topology.setBolt("score", new SentimentScoringBolt(), 4)
                .shuffleGrouping("join");

      topology.setBolt("hdfs", new HDFSBolt(), 4)
                .shuffleGrouping("score");
         topology.setBolt("nodejs", new NodeNotifierBolt(), 4)
                .shuffleGrouping("score");*/

        return topology.createTopology();
    }

    private static Config createConfig(boolean local)
    {
        int workers = Properties.getInt("rts.storm.workers");
        Config conf = new Config();
        conf.setDebug(true);
        if (local)
            conf.setMaxTaskParallelism(workers);
        else
            conf.setNumWorkers(workers);
        return conf;
    }
}
