package group.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
	public static Map<Integer, Article> articles = new HashMap<Integer, Article>();
	public static Map<String, Integer> indexMap = new HashMap<String, Integer>();
	static int LastID = 0;

	public static Map<Integer, Article> extractFiles( String folderPath ) throws Exception
	{
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				extract(folderPath + "\\" + listOfFiles[i].getName());

			} 
			indexMap.put(listOfFiles[i].getName(), LastID);
		}
		BufferedWriter out = new BufferedWriter( 
				new FileWriter("extract.txt")); 
		out.write(articles.toString()); 
		out.close(); 
		return articles;
	}

	private static void extract(String fileName) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.ISO_8859_1);
		String section = "";

		for(String line : lines) {
			if(line.contains("<REUTERS")) {
				section = line;
			} else if(line.contains("</REUTERS>")) {
				section = section + line;
				constructArticle(section);
			} else {
				section = section + line;
			}
		}
	}

	private static void constructArticle(String section) throws ParseException {
		int idStart = section.indexOf("NEWID=\"") + 7;
		int id = Integer.parseInt(section.substring(idStart, section.indexOf("\"", idStart)));	
		int bodyIndex = section.indexOf("<BODY>");
		if(bodyIndex == -1) {
			return;
		}
		String body = section.substring(section.indexOf("<BODY>") + 6, section.indexOf("</BODY>"));
		String title = section.substring(section.indexOf("<TITLE>") + 7, section.indexOf("</TITLE>"));
		String date = section.substring(section.indexOf("<DATE>") + 6, section.indexOf("</DATE>"));

		Article article = new Article(id, title, new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(date), body);
		LastID = id;
		articles.put(id, article);
	}
}
