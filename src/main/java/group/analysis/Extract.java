package group.analysis;

import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Extract {

	public static void main( String[] args ) throws Exception
	{
		Map<Integer, Article> articles = Parser.extractFiles("C:\\Users\\19025\\Desktop\\winter 2019\\Data collection\\reuters21578_Assignment 3\\reuters21578_Assignment 3");
		MongoClient mongo = new MongoClient( "localhost" , 27017 ); 
		MongoDatabase database = mongo.getDatabase("reuters_data");
		MongoCollection<Document>  collection = database.getCollection("articles"); 
		for(Entry<Integer, Article> doc: articles.entrySet()) {
			Document document = new Document();
			Article article = doc.getValue();
			document.put("ID", article.ID);
			document.put("Date", article.date);
			document.put("Title", article.title);
			document.put("content", article.content);
			collection.insertOne(document);
		}
		mongo.close();
		System.out.println("Extracted and uploaded " + articles.size() + " documents succesfully");
	}
}
