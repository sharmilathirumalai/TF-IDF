package group.analysis;

import java.util.Date;

public class Article {
	int ID;
	String title;
	Date date;
	String content;

	Article(int ID, String title, Date date,String content) {
		this.ID = ID;
		this.title = title;
		this.date = date;
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public String toString() { 
	    return "ID: '" + this.ID + "', content: '" + this.content;
	} 
}
