package com.zdatainc.rts.storm;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
//!import cmu.arktweetnlp.POSTagger;
//!import cmu.arktweetnlp.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.IIOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;



import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author girish
 */
public class DtreeBolt extends BaseBasicBolt implements Serializable{

	
    static File file = new File("/home/cloudera/group3/DTreeStorm/DtreeResult.txt");
    
     

    
   //! transient MaxentTagger tagger;
    
    
    public DtreeBolt()
    {
    	
    	
    }
    
    
    


	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		// TODO Auto-generated method stub
	     
		
		menu();
		
		
	}
	
	
	   public Map<String, Object> getComponenetConfiguration() { return null; }





	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("tweet_id", "tweet_text"));		
	}
	


	public static void menu() {
		
		

		//TreeModel treeFile;
		DataSource trainingFile;
		DataSource testingFile;

		try {
			trainingFile = new DataSource("/home/cloudera/group3/DTreeStorm/train.arff");
			testingFile = new DataSource("/home/cloudera/group3/DTreeStorm/test.arff");
			trainTest(trainingFile, testingFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}




	private static void trainTest(DataSource trainingFile, DataSource testingFile) {

		
	 	FileWriter fw = null;
        BufferedWriter bw = null;
    	try {
    	if (!file.exists()) {
			
				file.createNewFile();
    	}
			
		 fw = new FileWriter(file.getAbsoluteFile(),true);
		 bw = new BufferedWriter(fw);
		 
		
		 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		J48 tree = new J48();

		try {
			Instances train = trainingFile.getDataSet();
			Instances test = testingFile.getDataSet();
			train.setClassIndex(train.numAttributes() - 1);
			test.setClassIndex(test.numAttributes() - 1);

			tree.buildClassifier(train);
			Evaluation eval = new Evaluation(train);

 			eval.evaluateModel(tree, test);

 			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
 			System.out.println(eval.toMatrixString());
 			
 			bw.write("\nResults\n======\n");
 			bw.write(eval.toMatrixString());
 			bw.close();

		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	
}    

