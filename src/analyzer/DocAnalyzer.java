/**
 * 
 */
package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import structures.Post;

/**
 * @author hongning
 * Sample codes for demonstrating OpenNLP package usage 
 * NOTE: the code here is only for demonstration purpose, 
 * please revise it accordingly to maximize your implementation's efficiency!
 */
public class DocAnalyzer {
	
	//a list of stopwords
	HashSet<String> m_stopwords;
	
	//you can store the loaded reviews in this arraylist for further processing
	ArrayList<Post> m_reviews;
	
	//you might need something like this to store the counting statistics for validating Zipf's and computing IDF
	//HashMap<String, Token> m_stats;	
	
	//we have also provided sample implementation of language model in src.structures.LanguageModel
	Tokenizer m_tokenizer;
	ArrayList<HashMap<String, Integer>> tf; // term frequencies for each review
	HashMap<String, Integer> df; // document frequency table
	public DocAnalyzer() throws InvalidFormatException, FileNotFoundException, IOException {
		m_reviews = new ArrayList<Post>();
		tf = new ArrayList<HashMap<String, Integer>>();
		df = new HashMap<String, Integer>();
		m_tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
	}
	
	//sample code for loading a list of stopwords from file
	//you can manually modify the stopword file to include your newly selected words
	public void LoadStopwords(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;

			while ((line = reader.readLine()) != null) {
				//it is very important that you perform the same processing operation to the loaded stopwords
				//otherwise it won't be matched in the text content
				line = SnowballStemmingDemo(NormalizationDemo(line));
				if (!line.isEmpty())
					m_stopwords.add(line);
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n", m_stopwords.size(), filename);
		} catch(IOException e){
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}
	
	public void analyzeDocument(JSONObject json) {		
		try {
			JSONArray jarray = json.getJSONArray("Reviews"); // change
			for(int i=0; i<jarray.length(); i++) {
				Post review = new Post(jarray.getJSONObject(i));
				/**
				 * HINT: perform necessary text processing here, e.g., tokenization, stemming and normalization
				 */
				String[] tokens = m_tokenizer.tokenize(review.getContent());
				HashMap<String, Integer> document_tf = new HashMap<String, Integer>();
				for(String token : tokens) {
					if(!document_tf.containsKey(token))
						document_tf.put(token, 1);
					else
						document_tf.put(token, document_tf.get(token) + 1);
				}
				tf.add(document_tf);
				for(String token : document_tf.keySet()) {
					if(!df.containsKey(token))
						df.put(token, 1);
					else
						df.put(token, df.get(token) + 1);
				}
				m_reviews.add(review);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	//sample code for loading a json file
	public JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			
			while((line=reader.readLine())!=null) {
				buffer.append(line);
			}
			reader.close();
			
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	// sample code for demonstrating how to recursively load files in a directory 
	public void LoadDirectory(String folder, String suffix) {
		File dir = new File(folder);
		int size = m_reviews.size();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				analyzeDocument(LoadJson(f.getAbsolutePath()));
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		size = m_reviews.size() - size;
		System.out.println("Loading " + size + " review documents from " + folder);
	}

	//sample code for demonstrating how to use Snowball stemmer
	public String SnowballStemmingDemo(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	//sample code for demonstrating how to perform text normalization
	public String NormalizationDemo(String token) {
		// remove all non-word characters
		// please change this to removing all English punctuation
		token = token.replaceAll("\\W+", ""); 
		
		// convert to lower case
		token = token.toLowerCase(); 
		
		// add a line to recognize integers and doubles via regular expression
		// and convert the recognized integers and doubles to a special symbol "NUM"
		
		return token;
	}
	
	public void TokenizerDemon(String text) {
		try {
			
			/**
			 * HINT: instead of constructing the Tokenizer instance every time when you perform tokenization,
			 * construct a global Tokenizer instance once and evoke it everytime when you perform tokenization.
			 */
			Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
			
			System.out.format("Token\tNormalization\tSnonball Stemmer\tPorter Stemmer\n");
			for(String token:tokenizer.tokenize(text)){
				System.out.format("%s\t%s\t%s\t%s\n", token, NormalizationDemo(token), SnowballStemmingDemo(token), PorterStemmingDemo(token));
			}
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException {		
		DocAnalyzer analyzer = new DocAnalyzer();
		
		//codes for demonstrating tokenization and stemming
		analyzer.TokenizerDemon("I've practiced for 30 years in pediatrics, and I've never seen anything quite like this.");
		
		//codes for loading json file
		//analyzer.analyzeDocumentDemo(analyzer.LoadJson("./data/Samples/query.json"));
		
		//when we want to execute it in command line
		String dir_name = "./data/Samples";
		analyzer.LoadDirectory(dir_name, ".json");
	}

}