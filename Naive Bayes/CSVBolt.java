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

public class CSVBolt extends BaseBasicBolt
{
    private static final long serialVersionUID = 42L;
    private static final Logger LOGGER =
        Logger.getLogger(CSVBolt.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    //!File file = new File("/home/group3/Bruno/Challenge2/StormSampleProject/rts.storm/testing.csv");
    File file = new File("/home/cloudera/group3/shen_testing.csv");


	FileWriter fw = null;
    BufferedWriter bw = null;
    int flag=0;
    
    
    
    public void writeCSVtitle()
    {
    	 
        try {
       	 
        	if(file.exists())
        	{
        		file.delete();
        		file.createNewFile();
        	}
        	
			fw = new FileWriter(file.getAbsoluteFile(),true);
			 bw = new BufferedWriter(fw);
			 
			 bw.append("Tweet");
      		 bw.append(",");
      		 bw.append("Sentiment");
      		 bw.close();
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet_id", "tweet_text"));
       
    }

    public void execute(Tuple input, BasicOutputCollector collector)
    {
    	
         
         
         try {
        	 
			fw = new FileWriter(file.getAbsoluteFile(),true);
			 bw = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	
    	long id = 0;
        String text = null;
             
      
        LOGGER.debug("filttering incoming tweets");
        String json = input.getString(0);
        try {
			bw.write(json);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
      try
        {
            JsonNode root = mapper.readValue(json, JsonNode.class);
            
        
                if (root.get("id") != null && root.get("text") != null)
                {
                    id = root.get("id").longValue();
                    text = root.get("text").textValue();

                   
                  
                    bw.append("\n");
                    bw.append(text+","+"?");
                    
                    
                    bw.close();
                    
                   
                }
                else
                    LOGGER.debug("tweet id and/ or text was null");
            
          
        }
        catch (IOException ex)
        {
            LOGGER.error("IO error while filtering tweets", ex);
            LOGGER.trace(null, ex);
        }
        
        
   
        collector.emit(new Values("test","test"));
        
    }

    public Map<String, Object> getComponenetConfiguration() { return null; }
    
    
 
    
  
}
