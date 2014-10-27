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

import tfidf.MutableInt;
import tfidf.TfIdf;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.BinarySparseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;

/**
 *
 * @author girish
 */
public class WekaTutorial extends BaseBasicBolt implements Serializable{
    
    private ArrayList<String> featureWords;
    private ArrayList<Attribute> attributeList;
    private Instances inputDataset;
  //!  private POSTagger posTagger;
    private Classifier classifier;
    private ArrayList<String> sentimentClassList;
    private Instances testingInstances0;
    private  Instances testingInstances;
    File file = new File("/home/cloudera/output.txt");
    
    File file2 = new File("/home/cloudera/output2.txt");
     
    
    Fields _outFields;
    private static final long serialVersionUID = 42L;
    private static final Logger LOGGER =
        Logger.getLogger(WekaTutorial.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    BufferedReader br = null;
    
    SortedMap<Instance,Instance> map = new TreeMap<Instance,Instance>();
    
    
    String URL_REGEX="((www\\.[\\s]+)|(http?://[^\\s]+))";
    
    String CONSECUTIVE_CHARS="([a-z])\\1{1,}";
    
    String STARTS_WITH_NUMBER="[1-9]\\s*(\\w+)";
    
   
    
    List<String> documents = new ArrayList<String>();
    
    
   //! transient MaxentTagger tagger;
    
    
    public WekaTutorial()
    {
    	
    	 attributeList = new ArrayList<>();
    	 
    /*	 documents.add("joly");
    	 documents.add("excited");
    	 documents.add("thejep");
    	 documents.add("joy");
    	 documents.add("hope");
    	 documents.add("life");
    	 documents.add("god");
    	 documents.add("lovely");
    	 documents.add("friends");
    	 documents.add("disgust");
    	 documents.add("bc4");
    	 documents.add("sad");
    	 documents.add("studentloan");
    	 documents.add("unhappy");
    	 documents.add("cry");*/
    	 
    	 documents.add("love");
    	 documents.add("that");
    	 documents.add("I");
    	 documents.add("can");
    	 documents.add("love");
    /*	  try {
			tagger = new MaxentTagger("taggers/left3words-wsj-0-18.tagger");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
      //!   posTagger = new POSTagger();
        
   /*     FileWriter fw = null;
        BufferedWriter bw = null;
    	try {
    	if (!file.exists()) {
			
			 file.createNewFile();
    	}
			
		 fw = new FileWriter(file.getAbsoluteFile(),true);
		 bw = new BufferedWriter(fw);
		 bw.write("before init");
		 bw.close();
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   */
                
      initialize();
    }
    
    
    private void initialize()
    {
    	
    /*	 FileWriter fw = null;
         BufferedWriter bw = null;
     	try {
     	if (!file.exists()) {
 			
 				file.createNewFile();
     	}
 			
 		 fw = new FileWriter(file.getAbsoluteFile(),true);
 		 bw = new BufferedWriter(fw);
 		 bw.write("init");
 		 bw.close();
 		
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}   */
    	
        ObjectInputStream ois = null;
        try {
            //reads the feature words list to a hashset
            ois = new ObjectInputStream(new FileInputStream("/home/cloudera/group3/FeatureWordsList.dat"));
            featureWords = (ArrayList<String>) ois.readObject();
        } catch (Exception ex) {
            System.out.println("Exception in Deserialization");
        } finally {
            try {
                ois.close();
            } catch (IOException ex) {
                System.out.println("Exception while closing file after Deserialization");
            }
        }
        
        //creating an attribute list from the list of feature words
        sentimentClassList = new ArrayList<>();
        sentimentClassList.add("positive");
        sentimentClassList.add("negative");
        for(String featureWord : featureWords)
        {
            attributeList.add(new Attribute(featureWord));
        }
        //the last attribute reprsents ths CLASS (Sentiment) of the tweet
        attributeList.add(new Attribute("Sentiment",sentimentClassList)); 
        
        
     
    }
    
    
    public void trainClassifier(final String INPUT_FILENAME)
    {
    	
    	/* FileWriter fw = null;
         BufferedWriter bw = null;
     	try {
     	if (!file.exists()) {
 			
 				file.createNewFile();
     	}
 			
 		 fw = new FileWriter(file.getAbsoluteFile(),true);
 		 bw = new BufferedWriter(fw);
 		 bw.write("trainClassifier");
 		 bw.close();
 		
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  */
    	
            getTrainingDataset(INPUT_FILENAME);
            
            //trainingInstances consists of feature vector of every input
            Instances trainingInstances = createInstances("TRAINING_INSTANCES");
                 for(Instance currentInstance : inputDataset)
            {
                //extractFeature method returns the feature vector for the current input
                Instance currentFeatureVector = extractFeature(currentInstance);
                
                //Make the currentFeatureVector to be added to the trainingInstances
                currentFeatureVector.setDataset(trainingInstances);
                trainingInstances.add(currentFeatureVector);
            }
            
        //You can create the classifier that you want. In this tutorial we use NaiveBayes Classifier
        //For instance classifier = new SMO;
                 
          classifier = new NaiveBayes();
          //!     classifier = new SMO();
            
        try {
            //classifier training code
            classifier.buildClassifier(trainingInstances);
            
            //storing the trained classifier to a file for future use
         weka.core.SerializationHelper.write("NaiveBayes.model",classifier);
         //!       weka.core.SerializationHelper.write("SVM.model",classifier);
        } catch (Exception ex) {
            System.out.println("Exception in training the classifier.");
        }
    }
    
   
    
    
    public void testClassifier(final String INPUT_FILENAME)
    {
    	
    	Random ran = new Random();
    	
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
    	
     	    getTrainingDataset(INPUT_FILENAME);
            
        //trainingInstances consists of feature vector of every input
        Instances testingInstances = createInstances("TESTING_INSTANCES");
 
        
           for(Instance currentInstance : inputDataset)
        {
            //extractFeature method returns the feature vector for the current input
            Instance currentFeatureVector = extractFeature(currentInstance);

            //Make the currentFeatureVector to be added to the trainingInstances
            currentFeatureVector.setDataset(testingInstances);
            testingInstances.add(currentFeatureVector);
            
    
        }
            
     
		
       try {
		classifier = (Classifier) weka.core.SerializationHelper.read("NaiveBayes.model");
		//!  classifier = (Classifier) weka.core.SerializationHelper.read("SVM.model");
	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
            
       try {
            //Classifier deserialization
    	   
    	   String row = "row1";
    	   String columnFamily="Sentiment";
    	   String column="col";
    	   
            //classifier testing code
            for(Instance testInstance : testingInstances)
           {
            	int count =ran.nextInt(10000);
                
            	
              double score = classifier.classifyInstance(testInstance);
                System.out.println(testingInstances.attribute("Sentiment").value((int)score));
              //  bw.write(testInstance.stringValue(1)+","+testingInstances.attribute("Sentiment").value((int)score)+"\n");
              //  bw.write(testingInstances.attribute("Sentiment").value((int)score));
                String value = testingInstances.attribute("Sentiment").value((int)score);
                //bw.write(testingInstances.attribute("Sentiment").value((int)score)+"\n");
               // bw.close();
              try{
                DefaultHttpClient httpClient = new DefaultHttpClient();
                
                HttpGet getRequest = new HttpGet("http://134.193.136.114:8181/HBaseWS2/jaxrs/generic" +
                		"/hbaseInsertIndividual/ResultTableTestGrp3/"+row+"/"+columnFamily+"/"+column+count+"/"+value);
                
                HttpResponse response = httpClient.execute(getRequest);
                
                if(response.getStatusLine().getStatusCode()!=200){
                	
                	throw new RuntimeException("Failed: HTTP error code :"+response.getStatusLine().getStatusCode());
                	
                }
                
                BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
                
                String output;
                
                while((output = br.readLine())!=null){
                	System.out.println(output);
                }
                
                httpClient.getConnectionManager().shutdown();
                
             //   count = count + 1;
              }catch(ClientProtocolException e){
            	  e.printStackTrace();
              }catch (IOException e){
            	  e.printStackTrace();
              }
              
            }
        } catch (Exception ex) {
        	
            System.out.println("Exception in testing the classifier.");
          
       }
    }
    
    
    
    
    private void getTrainingDataset(final String INPUT_FILENAME)
    {
        try{
            //reading the training dataset from CSV file
            CSVLoader trainingLoader =new CSVLoader();
            trainingLoader.setSource(new File(INPUT_FILENAME));
            inputDataset = trainingLoader.getDataSet();
        }catch(IOException ex)
        {
            System.out.println("Exception in getTrainingDataset Method");
        }
    }
    
    
    private Instances createInstances(final String INSTANCES_NAME)
    {
        
        //create an Instances object with initial capacity as zero 
        Instances instances = new Instances(INSTANCES_NAME,attributeList,0);
        
        //sets the class index as the last attribute (positive or negative)
        instances.setClassIndex(instances.numAttributes()-1);
            
        return instances;
    }
    
    
    private Instance extractFeature(Instance inputInstance)
    {
    	///////////////////////////
		
     	/*FileWriter fw = null;
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
		}*/
    	
    	///////////////////
    	
    	Map<Integer,Double> featureMap = new TreeMap<>();
    	
    	String tweet = "";
    	
    	//Remove urls
    	tweet = inputInstance.stringValue(0).replaceAll(URL_REGEX,"");
    	
    	//Remove @username
    	tweet = tweet.replaceAll("@([^\\s]+)","");
    	
    	//Remove character repetition
    	tweet = tweet.replaceAll(CONSECUTIVE_CHARS, "$1");
    	
    	//Remove words starting with a number
    	tweet = tweet.replaceAll(STARTS_WITH_NUMBER,"");
    	
    	//Escape HTML
    	tweet = tweet.replaceAll("&amp;","&");
    	tweet = StringEscapeUtils.unescapeHtml(tweet);
    	
       
        StringTokenizer st = new StringTokenizer(tweet);
        
        while (st.hasMoreElements()) {
			String element = st.nextElement().toString();
			if(element.contains("#"))
			{
				element = element.replace("#", "");
				
				
				
			}
			
			 if(featureWords.contains(element))
		        {
		            //adding 1.0 to the featureMap represents that the feature word is present in the input data
		            featureMap.put(featureWords.indexOf(element),1.0);
		        }			
		}
        
 
       
        
        int indices[] = new int[featureMap.size()+1];
        double values[] = new double[featureMap.size()+1];
        int i=0;
        for(Map.Entry<Integer,Double> entry : featureMap.entrySet())
        {
            indices[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        indices[i] = featureWords.size();
        values[i] = (double)sentimentClassList.indexOf(inputInstance.stringValue(1));
        
        
        
        
        return new SparseInstance(1.0,values,indices,featureWords.size());
    }


	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		// TODO Auto-generated method stub
	     
		/* csv = new File ("/home/cloudera/group3/shen_testing.csv");
		
		if(csv.length()>0){*/
		
		trainClassifier("/home/cloudera/group3/shen_training.csv");

		testClassifier("/home/cloudera/group3/shen_testing.csv");
		
		//Runtfidf();
		//}
	}
	
	
	public void Runtfidf()
	{
	
		
		
	//	tf();
		
		//df();
		
//		idf();
		
		//Map<String, Double> map = tfidf();
		
		Map<String, Double> map =  tfidf_tweak1();
		
	
		
		FileWriter fw = null;
        BufferedWriter bw = null;
    	try {
    	if (!file.exists()) {
			
				file.createNewFile();
    	}
			
		 fw = new FileWriter(file.getAbsoluteFile(),true);
		 bw = new BufferedWriter(fw);
		// bw.write(map.size());
		 
		 for(Map.Entry<String,Double> entry : map.entrySet())
		 {//bw.write("ccc");
		 
			 bw.write(entry.getKey()+":"+entry.getValue()+"\n");
		 }
			bw.close();
		 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		  declarer.declare(new Fields("tweet_id", "tweet_text"));
	}
    
    
	public Map<String, Object> getComponenetConfiguration() { return null; }
	

	
	List<String> build_words(String document) {
	    List<String> wordList = new ArrayList<String>();
	    Matcher matcher = Pattern.compile("[a-zA-Z]+").matcher(document);
	    while (matcher.find()) wordList.add(matcher.group());
	    return wordList;
	  }

	  /**
	   * Calculates the Tf scores of words which are built by build_words.
	   *
	   * @return map that contains the words and their Tf scores.
	   * @see tfidf.akgul.TfIdf#build_words(String)
	   */
	  public Map<String, MutableInt> tf() {
	    Map<String, MutableInt> map = new HashMap<String, MutableInt>();
	    for (String document : documents) {
	      List<String> split = build_words(document);
	      for (String s : split) {
	        MutableInt counter = map.get(s);
	        if (counter == null) map.put(s, MutableInt.createMutableInt());
	        else counter.increment();
	      }
	    }
	    return map;
	  }

	  /**
	   * Calculates the Tf scores of words of a single document. Words are split
	   * by spaces. This should fixed with a smarter REGEX.
	   *
	   * @param document Single document to calculate the Tf scores of words of it.
	   * @return map that contains the words and Tf scores.
	   */
	  public Map<String, MutableInt> tf(String document) {
	    Map<String, MutableInt> map = new HashMap<String, MutableInt>();
	    String[] split = document.split(" ");
	    for (String s : split) {
	      MutableInt counter = map.get(s);
	      if (counter == null) map.put(s, MutableInt.createMutableInt());
	      else counter.increment();
	    }
	    return map;
	  }

	  /**
	   * This is the same method as calculating the tf scores. I don't know why I've
	   * written the same thing again.
	   *
	   * @param list2 List that contains the words that are split by space.
	   * @return map that contains the words and their tf scores.
	   * @see tfidf.akgul.TfIdf#tf()
	   */
	  private Map<String, MutableInt> counter(final List<String> list2) {
	    Map<String, MutableInt> map = new HashMap<String, MutableInt>();
	    for (String s : list2) {
	      MutableInt counter = map.get(s);
	      if (counter == null) map.put(s, MutableInt.createMutableInt());
	      else counter.increment();
	    }
	    return map;
	  }

	  /**
	   * Calculates the df scores of the words in documents.
	   *
	   * @return Map that contains the words and their df scores.
	   */
	  public Map<String, MutableInt> df() {
	    List<String> list2 = new ArrayList<String>();
	    Set<String> set = new HashSet<String>();
	    for (String s : documents) {
	      for (String d : build_words(s)) set.add(d);
	      for (String d : set) list2.add(d);
	      set.clear();
	    }
	    return counter(list2);
	  }

	  /**
	   * Calculates the df scores of the words in given documents documents.
	   *
	   * @param documents Document documents to calculate df scores.
	   * @return Map that contains the words and their df scores.
	   */
	  public Map<String, MutableInt> df(List<String> documents) {
	    List<String> list2 = new ArrayList<String>();
	    Set<String> set = new HashSet<String>();
	    for (String s : documents) {
	      for (String d : build_words(s)) set.add(d);
	      for (String d : set) list2.add(d);
	      set.clear();
	    }
	    return counter(list2);
	  }

	  /**
	   * Calculates the idf scores of words.
	   *
	   * @return Map that contains the words and their idf scores.
	   */
	  public Map<String, Double> idf() {
	    Map<String, MutableInt> map = df();
	    Map<String, Double> map2 = new HashMap<String, Double>();
	    for (String s : documents) {
	      List<String> split = build_words(s);
	      for (String d : split) {
	        if (map.get(d) != null) {
	          double val = 1.0 / (map.get(d).getCounter() + 1e-100);
	          map2.put(d, val);
	        }
	      }
	    }
	    return map2;
	  }

	  /**
	   * Calculates the tfidf scores of words. Basically, calculating the tfidf scores
	   * gives us the most important words of documents. This method has quite a few
	   * weakness. For example; this method gives very high scores for very rare words.
	   * For more accuracy, @see TfIdf#tfidf_tweak1.
	   *
	   * @return Map that contains the words and their tfidf scores.
	   * @see tfidf.akgul.TfIdf#tfidf_tweak1
	   */
	  public Map<String, Double> tfidf() {
	    Map<String, MutableInt> df_scores = df();
	    Map<String, Double> map = new HashMap<String, Double>();
	    for (String s : documents) {
	      List<String> split = build_words(s);
	      Map<String, MutableInt> tf_scores = tf(s);
	      for (String d : split) {
	        if (map.get(d) == null) {
	        	     double score = tf_scores.get(d).getCounter() /  (df_scores.get(d).getCounter() + 0.01);
	        	     
	    map.put(d, score);
	        }
	      }
	    }
	    return map;
	  }

	  /**
	   * Calculates the tfidf scores of words. This method also finds out the most
	   * important words of given documents. However, this is more intelligent than
	   * then pure TfIdf#tfidf method.
	   *
	   * @return Map that contains the words and their tweaked tfidf scores.
	   * @see tfidf.akgul.TfIdf#tfidf()
	   */
	  public Map<String, Double> tfidf_tweak1() {
	    Map<String, MutableInt> df_scores = df();
	    final int N = documents.size();
	    Map<String, Double> map = new HashMap<String, Double>();
	    for (String s : documents) {
	      List<String> split = build_words(s);
	      Map<String, MutableInt> tf_scores = tf(s);
	      for (String d : split) {
	        if (map.get(d) == null) {
	          double score = tf_scores.get(d).getCounter() * (Math.log(
	              N / df_scores.get(d).getCounter()
	          ) + 0.01);
	          map.put(d, score);
	        }
	      }
	    }
	    return map;
	  }
}    

