package com.zdatainc.rts.storm;


import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.BinarySparseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
//import cmu.arktweetnlp.POSTagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.BinarySparseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;

public class ARFFBolt extends BaseBasicBolt
{
    private static final long serialVersionUID = 42L;
    private static final Logger LOGGER =
        Logger.getLogger(ARFFBolt.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    File file = new File("/home/cloudera/group3/DTreeStorm/test.arff");

	 FileWriter fw = null;
    BufferedWriter bw = null;
    int flag=0;
    
    
    
    
    
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet_id", "tweet_text"));
       
    }

    public void execute(Tuple input, BasicOutputCollector collector)
    {
         
         try {
        	 
			fw = new FileWriter(file.getAbsoluteFile(),true);
			 bw = new BufferedWriter(fw);
				
		    	bw.write(input.getString(0));
		    	bw.close();
		        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
        
   
        collector.emit(new Values("test","test"));
        
    }

    public Map<String, Object> getComponenetConfiguration() { return null; }
    
    
 
    
  
}
