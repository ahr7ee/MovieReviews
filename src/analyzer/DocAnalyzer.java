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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jdk.internal.org.xml.sax.SAXException;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.porterStemmer;
import org.w3c.dom.Document;

import com.sun.javafx.collections.MappingChange.Map;

/**
 * @author hongning Sample codes for demonstrating OpenNLP package usage NOTE:
 *         the code here is only for demonstration purpose, please revise it
 *         accordingly to maximize your implementation's efficiency!
 */
public class DocAnalyzer {

	// a list of stopwords
	HashSet<String> m_stopwords;

	// you can store the loaded reviews in this arraylist for further processing
	ArrayList<String> m_reviews;
	ArrayList<String> movie_names;
	ArrayList<ArrayList<Double>> tf_idf;
	ArrayList<String> vocab;
	// you might need something like this to store the counting statistics for
	// validating Zipf's and computing IDF
	// HashMap<String, Token> m_stats;

	// we have also provided sample implementation of language model in
	// src.structures.LanguageModel
	Tokenizer m_tokenizer;
	ArrayList<HashMap<String, Integer>> tf; // term frequencies for each review
	HashMap<String, Integer> df; // document frequency table

	public DocAnalyzer() throws InvalidFormatException, FileNotFoundException,
			IOException {
		m_reviews = new ArrayList<String>();
		tf = new ArrayList<HashMap<String, Integer>>();
		df = new HashMap<String, Integer>();
		m_tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream(
				"./data/Model/en-token.bin")));
		vocab = new ArrayList<String>();
		movie_names = new ArrayList<String>();
	}

	// sample code for loading a list of stopwords from file
	// you can manually modify the stopword file to include your newly selected
	// words
	public void LoadStopwords(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			String line;

			while ((line = reader.readLine()) != null) {
				// it is very important that you perform the same processing
				// operation to the loaded stopwords
				// otherwise it won't be matched in the text content
				line = SnowballStemmingDemo(NormalizationDemo(line));
				if (!line.isEmpty())
					m_stopwords.add(line);
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n",
					m_stopwords.size(), filename);
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}

	public void analyzeDocument(String text) {
		/**
		 * HINT: perform necessary text processing here, e.g., tokenization,
		 * stemming and normalization
		 */
		// to do: stemming
		String[] tokens = m_tokenizer.tokenize(text.toLowerCase());
		for (int j = 0; j < tokens.length; j++)
			tokens[j] = SnowballStemmingDemo(tokens[j]);
		HashMap<String, Integer> document_tf = new HashMap<String, Integer>();
		for (String token : tokens) {
			if (!document_tf.containsKey(token))
				document_tf.put(token, 1);
			else
				document_tf.put(token, document_tf.get(token) + 1);
			if (!df.containsKey(token))
				df.put(token, 1);
			else
				df.put(token, df.get(token) + 1);
		}
		tf.add(document_tf);
		/*
		 * for(String token : document_tf.keySet()) { if(!df.containsKey(token))
		 * df.put(token, 1); else df.put(token, df.get(token) + 1); if(!) }
		 */
		m_reviews.add(text);
	}

	// sample code for loading a xml file
	public String LoadXML(String filename) throws org.xml.sax.SAXException,
			SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(true);
		// factory.
		factory.setIgnoringElementContentWhitespace(true);
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			File file = new File(filename);
			doc = builder.parse(file);
			// Do something with the document here.
		} catch (ParserConfigurationException e) {
		} catch (IOException e) {
		}
		return doc.getElementsByTagName("text").item(0).getFirstChild()
				.getNodeValue();
	}

	// sample code for demonstrating how to recursively load files in a
	// directory
	public void LoadDirectory(String folder, String suffix)
			throws org.xml.sax.SAXException, SAXException {
		File dir = new File(folder);
		int size = m_reviews.size();
		for (File f : dir.listFiles()) {
			String name = f.getName();
			if (f.isFile() && name.endsWith(suffix)) {
				movie_names.add(name.substring(0, name.length() - 4));
				analyzeDocument(LoadXML(f.getAbsolutePath()));
			} else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		size = m_reviews.size() - size;
		System.out.println("Loading " + size + " review documents from "
				+ folder);
	}

	// sample code for demonstrating how to use Snowball stemmer
	public String SnowballStemmingDemo(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}

	// sample code for demonstrating how to use Porter stemmer
	public String PorterStemmingDemo(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}

	// sample code for demonstrating how to perform text normalization
	public String NormalizationDemo(String token) {
		// remove all non-word characters
		// please change this to removing all English punctuation
		token = token.replaceAll("\\W+", "");

		// convert to lower case
		token = token.toLowerCase();

		// add a line to recognize integers and doubles via regular expression
		// and convert the recognized integers and doubles to a special symbol
		// "NUM"

		return token;
	}

	public double tf(String t, int d) {
		if (!tf.get(d).containsKey(t))
			return 0;
		int raw_tf = tf.get(d).get(t);
		return 1 + Math.log(raw_tf);
	}

	public double df(String t) {
		double N = tf.size();
		int n_t = df.get(t);
		return Math.log(N / n_t);
	}

	public double tf_idf(String t, int d) {
		return tf(t, d) * df(t);
	}

	public ArrayList<Double> tf_idf(int d) {
		ArrayList<Double> temp = new ArrayList<Double>();
		for (String t : vocab)
			temp.add(tf_idf(t, d));
		return temp;
	}

	public ArrayList<ArrayList<Double>> tf_idf() {
		ArrayList<ArrayList<Double>> temp = new ArrayList<ArrayList<Double>>();
		for (int d = 0; d < tf.size(); d++)
			temp.add(tf_idf(d));
		return temp;
	}

	public static double magnitude(ArrayList<Double> tf_idf) {
		double sum = 0;
		for (int i = 0; i < tf_idf.size(); i++)
			sum += Math.pow(tf_idf.get(i), 2);
		return Math.sqrt(sum);
	}

	public static double dotProduct(ArrayList<Double> tf_idf1,
			ArrayList<Double> tf_idf2) {
		double sum = 0;
		for (int i = 0; i < tf_idf1.size(); i++)
			sum += tf_idf1.get(i) * tf_idf2.get(i);
		return sum;
	}

	public static double cosineSimilarity(ArrayList<Double> tf_idf1,
			ArrayList<Double> tf_idf2) {
		double mag1 = magnitude(tf_idf1);
		double mag2 = magnitude(tf_idf2);
		if (mag1 * mag2 == 0)
			return 0;
		return dotProduct(tf_idf1, tf_idf2)
				/ (magnitude(tf_idf1) * magnitude(tf_idf2));
	}

	public ArrayList<Double> queryVector(String query) {
		ArrayList<Double> temp = new ArrayList<Double>();
		String[] tokens = m_tokenizer.tokenize(query.toLowerCase());
		HashSet<String> unigrams = new HashSet<String>();
		for (int i = 0; i < tokens.length; i++)
			unigrams.add(SnowballStemmingDemo(tokens[i]));
		// bag of words model for query
		for (String t : df.keySet()) {
			if (unigrams.contains(t))
				temp.add(df(t));
			else
				temp.add(0.0);
		}
		return temp;
	}

	public String bestMatch(String query) {
		int bestIndex = 0;
		double bestSim = 0;
		ArrayList<Double> queryVec = queryVector(query);
		for (int i = 0; i < tf_idf.size(); i++) {
			double sim = cosineSimilarity(tf_idf.get(i), queryVec);
			if (sim > bestSim) {
				bestSim = sim;
				bestIndex = i;
			}
		}
		return m_reviews.get(bestIndex);
	}

	public HashSet<Integer> movieIndices(String name) {
		HashSet<Integer> temp = new HashSet<Integer>();
		for (int i = 0; i < movie_names.size(); i++)
			if (movie_names.get(i).equals(name))
				temp.add(i);
		return temp;
	}

	public ArrayList<Double> tf_idf_avg(String name) {
		HashSet<Integer> ix = movieIndices(name);
		Double[] temp = new Double[vocab.size()];
		for (int i : ix) {
			ArrayList<Double> movie_vec = tf_idf.get(i);
			for (int j = 0; j < movie_vec.size(); j++)
				temp[j] += movie_vec.get(j);
		}
		for (int i = 0; i < temp.length; i++)
			temp[i] /= Math.max(ix.size(), 1); // avoid division by 0
		return (ArrayList<Double>) Arrays.asList(temp);
	}

	public static void main(String[] args) throws InvalidFormatException,
			FileNotFoundException, IOException, org.xml.sax.SAXException,
			SAXException {
		DocAnalyzer analyzer = new DocAnalyzer();
		String dir_name = "./data/movies-data-v1.0/reviews";
		analyzer.LoadDirectory(dir_name, ".xml");
		int vocSize = 1000;
		analyzer.setVocabulary(vocSize);
		analyzer.tf_idf = analyzer.tf_idf(); // initialize tf-idf vectors
		String testQuery = "9";
		int numKeywords = 10;
		int moviesPerQuery = 5;
		analyzer.movieRecommendations(testQuery, numKeywords, moviesPerQuery);
	}

	public void movieRecommendations(String name, int numKeywords,
			int moviesPerQuery) {
		ArrayList<Double> vec = tf_idf_avg(name);
		HashSet<String> keywords = getKeywords(vec, numKeywords);
		HashSet<String> movies = new HashSet<String>();
		for (String word : keywords) {
			HashSet<String> similarMovies = moviesQuery(word, moviesPerQuery);
			movies.addAll(similarMovies);
		}
		System.out.println(movies.toString());
	}

	private HashSet<String> moviesQuery(String word, int moviesPerQuery) {
		// TODO Auto-generated method stub
		HashMap<String, Double> relevance = new HashMap<String, Double>();
		int index = vocab.indexOf(word);
		for (String movie : movie_names) {
			ArrayList<Double> movieVec = tf_idf_avg(movie);
			relevance.put(movie, movieVec.get(index));
		}
		HashSet<String> mostRelevant = new HashSet<String>();
		while (mostRelevant.size() < moviesPerQuery) {
			double max = 0;
			String best = "";
			for (String movie : relevance.keySet()) {
				if (relevance.get(movie) > max && !mostRelevant.contains(movie)) {
					max = relevance.get(movie);
					best = movie;
				}
			}
			mostRelevant.add(best);
		}
		return mostRelevant;
	}

	private HashSet<String> getKeywords(ArrayList<Double> vec, int numKeywords) {
		HashSet<String> keywords = new HashSet<String>();
		while (keywords.size() < numKeywords) {
			double max = 0;
			String best = "";
			for (int i = 0; i < vocab.size(); i++) {
				String word = vocab.get(i);
				if (vec.get(i) > max && !keywords.contains(word)) {
					max = vec.get(i);
					best = word;
				}
			}
			keywords.add(best);
		}
		return keywords;
	}

	private void setVocabulary(int vocSize) {
		// TODO Auto-generated method stub
		ArrayList<String> chosen = new ArrayList<String>();
		while (chosen.size() < vocSize) {
			int max = 0;
			String best = "";
			for (String t : df.keySet()) {
				if (df.get(t) > max && !chosen.contains(t)) {
					max = df.get(t);
					best = t;
				}
			}
			chosen.add(best);
		}
		vocab = chosen;
	}
}