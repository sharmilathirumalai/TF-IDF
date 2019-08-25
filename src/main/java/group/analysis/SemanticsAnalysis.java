package group.analysis;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class SemanticsAnalysis {
	static Map<Integer, Map<String, Double>> termFrequency= new HashMap<Integer, Map<String, Double>>();
	static Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
	static Map<String, Double> inverseTermFrequency  = new HashMap<String, Double>();
	static Map<Integer, Double> docScore = new HashMap<Integer, Double>();

	public static void main( String[] args ) throws Exception
	{
		String query = "Canada";
		Map<Integer, Article> articles = new HashMap<Integer, Article>();

		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase database = mongo.getDatabase("reuters_data");
		MongoCollection<Document> collection = database.getCollection("articles");

		FindIterable<Document> iterDoc = collection.find(); 
		Iterator result = iterDoc.iterator();
		while (result.hasNext()) {
			Document row = (Document) result.next();
			int key = (Integer) row.get("ID");
			Article value = new Article(key, (String) row.get("Title"),  (Date) row.get("Date"), (String) row.get("content"));
			articles.put(key, value);
		}
		mongo.close();

		computeTF(articles);
		computeIDF();

		String[] queryWords = query.split("\\s+");
		Map<String, Integer> queryOccurence = new HashMap<String, Integer>();
		Map<String, Double> queryScore = new HashMap<String, Double>();
		Double queryDistance = 0.0;

		for(String word: queryWords) {
			queryOccurence.put(word.toLowerCase(), queryOccurence.getOrDefault(word, 0) + 1);
		}

		for(Entry<String, Integer> wordSet : queryOccurence.entrySet()) {
			String word = wordSet.getKey();
			int occurence = wordSet.getValue();
			int tf = wordFrequency.getOrDefault(word, 0);
			Double score =  (occurence * inverseTermFrequency.get(word)) / tf;
			queryScore.put(word, score);
			queryDistance = queryDistance + (score*score);
		}
		queryDistance = Math.sqrt(queryDistance);
		calculateDocDistance(queryScore, queryDistance);
		TreeMap<Integer, Double> sortedDocuments = sortMapByValue(docScore);
		writeToFile(sortedDocuments, articles);
		writeToConsole(sortedDocuments, articles);
	}

	private static void writeToConsole(TreeMap<Integer, Double> sortedDocuments, Map<Integer, Article> articles) {
		System.out.println(sortedDocuments);
		System.out.println("\n");
		Article topDoc = articles.get(sortedDocuments.firstKey());
		System.out.println("NewID: " + topDoc.ID);
		System.out.println("Date: " + topDoc.date);
		System.out.println("Title: " + topDoc.title);
		System.out.println("Article: " + topDoc.content);		
	}

	public static void writeToFile(TreeMap<Integer, Double> sortedDocuments, Map<Integer, Article> articles) throws IOException {
		FileWriter fileWriter = new FileWriter("output.txt");
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println(sortedDocuments);
		printWriter.println("\n");
		Article topDoc = articles.get(sortedDocuments.firstKey());
		printWriter.println("NewID: " + topDoc.ID);
		printWriter.println("Date: " + topDoc.date);
		printWriter.println("Title: " + topDoc.title);
		printWriter.println("Article: " + topDoc.content);
		printWriter.close();
	}

	public static TreeMap<Integer, Double> sortMapByValue(Map<Integer, Double> map){
		Comparator<Integer> comparator = new ValueComparator(map);
		TreeMap<Integer, Double> result = new TreeMap<Integer, Double>(comparator);
		result.putAll(map);
		return result;
	}


	private static void calculateDocDistance(Map<String, Double> queryScore, Double queryDistance) {	
		for(Entry<Integer, Map<String, Double>> document :  termFrequency.entrySet()) {
			Double distance = 0.000000;
			Double dotpdt = 0.0000000;
			for(Entry<String, Double> word : document.getValue().entrySet()) {
				Double tf = word.getValue();
				Double tfidf = tf * inverseTermFrequency.get(word.getKey());
				distance = distance + (tfidf*tfidf);
				dotpdt = dotpdt + (tfidf * queryScore.getOrDefault(word.getKey(), (double) 0));
			}
			distance = Math.sqrt(distance);
			Double cosine = dotpdt / (distance * queryDistance);
			docScore.put(document.getKey(), cosine);
		}
	}


	private static void computeIDF() {
		int documentsLength = termFrequency.size();
		for (Entry<String, Integer> word : wordFrequency.entrySet()) {
			Double idf = log2((double) documentsLength / word.getValue());
			inverseTermFrequency.put(word.getKey().toLowerCase(), idf);
		}
	}

	static Double log2(Double x)
	{
		return (Math.log(x) / Math.log(2));
	}


	private static void computeTF(Map<Integer, Article> articles) {
		for (Entry<Integer, Article> pair : articles.entrySet()) {
			String[] content = pair.getValue().getContent().split("\\s+");
			Map<String, Double> wordsCount= new HashMap<String, Double>();
			for(int  i=0; i< content.length; i++) {
				String word = content[i].toLowerCase();
				if(wordsCount.get(word) != null) {
					wordsCount.put(word, wordsCount.get(word) + 1);
				} else {
					wordsCount.put(word, wordsCount.getOrDefault(word, (double) 0) + 1);
					wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
				}

			}

			for (Entry<String, Double> word : wordsCount.entrySet()) {
				wordsCount.put(word.getKey(), word.getValue()/(content.length + 1));
			}
			termFrequency.put(pair.getKey(), wordsCount);
		}
	}

}

class ValueComparator implements Comparator<Integer>{

	Map<Integer, Double> map = new HashMap<Integer, Double>();

	public ValueComparator(Map<Integer, Double> map){
		this.map.putAll(map);
	}

	public int compare(Integer s1, Integer s2) {
		if(map.get(s1) >= map.get(s2)){
			return -1;
		} else{
			return 1;
		}	
	}
}

