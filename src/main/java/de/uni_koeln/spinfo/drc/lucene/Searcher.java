package de.uni_koeln.spinfo.drc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Searcher {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final StandardAnalyzer analyzer = new StandardAnalyzer();

	private int totalHits;

	private static final int NBEST = 100;

	public List<SearchResult> basicSearch(String indexDir, String q) throws IOException, ParseException {

		Directory dir = new SimpleFSDirectory(new File(indexDir).toPath());
		DirectoryReader dirReader = DirectoryReader.open(dir);
		IndexSearcher is = new IndexSearcher(dirReader);

		QueryParser parser = new QueryParser("contents", analyzer);
		Query query = parser.parse(q);
		logger.info("QUERY: " + query);

		List<SearchResult> resultList = new ArrayList<SearchResult>();
		TopDocs hits = is.search(query, NBEST);
		this.setTotalHits(hits.totalHits);
		
		for (int i = 0; i < hits.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = hits.scoreDocs[i];

			Document doc = is.doc(scoreDoc.doc);
			String filename = doc.get("url");
			String pageId = doc.get("pageId");
			String content = doc.get("contents");
			String language = doc.get("languages");
			String chapter = doc.get("chapters");
			String volume = doc.get("volume");
			
			SearchResult result = new SearchResult();
			result.setFilename(filename);
			result.setContent(content);
			result.setPageId(pageId);
			result.setLanguage(language);
			result.setChapter(chapter);
			result.setVolume(volume);

			resultList.add(result);
		}
		dirReader.close();
		return resultList;
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public List<SearchResult> search(String indexDir, String q) throws IOException, ParseException {

		Directory dir = new SimpleFSDirectory(new File(indexDir).toPath());
		DirectoryReader dirReader = DirectoryReader.open(dir);
		IndexSearcher is = new IndexSearcher(dirReader);

		QueryParser parser = new QueryParser("contents", analyzer);
		Query query = parser.parse(q);
		logger.info("QUERY: " + query);

		List<SearchResult> resultList = new ArrayList<SearchResult>();
		TopDocs hits = is.search(query, NBEST);
		this.setTotalHits(hits.totalHits);

		for (int i = 0; i < hits.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = hits.scoreDocs[i];
			Document doc = is.doc(scoreDoc.doc);

			SearchResult result = new SearchResult(doc.get("url"), doc.get("pageId"), doc.get("contents"),
					doc.get("languages"), doc.get("chapters"), doc.get("volume"));
			resultList.add(result);
		}
		dirReader.close();
		return resultList;
	}

}
