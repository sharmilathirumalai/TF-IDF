## TF-IDF
Retrieved the top most ranking document from [Reuters](http://kdd.ics.uci.edu/databases/reuters21578/reuters21578.html) dataset for the given query by using TF-IDF IR method.


## Data Extraction
Followed ETL (Extract, Transform and Load) method to extract data.

* Extract - Extracted each article from “SGM” files to be kept as separate documents by writing custom [parser](https://github.com/sharmilathirumalai/TF-IDF/blob/master/src/main/java/group/analysis/Parser.java#L39).
 Total number of documents extracted: **19,043 articles**
* Transform - Each document is converted to be in the form of 
```java
{
ID: <NewID>
Date: <Article Date>
Ttitle: <Article Title>
Content: <Article Body>
}
```
by [scraping](https://github.com/sharmilathirumalai/TF-IDF/blob/master/src/main/java/group/analysis/Parser.java#L55) the string.
* Load - Finally the transformed data is loaded into [Mongo DB](https://github.com/sharmilathirumalai/TF-IDF/blob/master/src/main/java/group/analysis/Extract.java#L19)

## Data Analysis - IR
 The  tf-idf score of each document is computed by adding the tf-idf score of title and content attribute in the document.	Calculated the cosine value and distance for each document and the query. By doing so, the top ranked document for the query canada is found to be as follows:
 
 ```json
 {
 "ID": 11751,
 "Date": "Tue Mar 31 18:38:11 AST 1987",
"Title": "COMINCO &lt;CLT> SELLS STAKE IN CANADA METAL",
"Article": "Cominco Ltd said itsold its 50 pct stake in Canada Metal Co Ltd to Canada Metalsenior management for an undisclosed sum.    Cominco said the sale was part of its previously announcedpolicy of divesting non-core businesses. Canada Metal is a Toronto-based producer of lead alloys andengineered lead products. Canada Metal production figures were not immediatelyavailable."
}
 ```
