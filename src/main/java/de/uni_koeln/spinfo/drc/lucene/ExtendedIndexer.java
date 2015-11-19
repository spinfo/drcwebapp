package de.uni_koeln.spinfo.drc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.spinfo.drc.mongodb.DataBase;
import de.uni_koeln.spinfo.drc.mongodb.data.document.Page;
import de.uni_koeln.spinfo.drc.mongodb.data.document.Word;
import de.uni_koeln.spinfo.drc.mongodb.repository.PageRepository;
import de.uni_koeln.spinfo.drc.util.PropertyReader;

//@Service()
public class ExtendedIndexer {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DataBase db;

	@Autowired
	PropertyReader propertyReader;

	private IndexWriter writer;

	public ExtendedIndexer() {
	}

	/*
	 * Der Indexer wird als Service initialisiert, wenn die App gestartet wird,
	 * sobald alle Services bereit stehen, kann die init aufgerufen werden.
	 */
	public void init() throws IOException {
		String indexDir = propertyReader.getIndexDir();
		Directory dir = new SimpleFSDirectory(new File(indexDir).toPath());
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
		this.writer = new IndexWriter(dir, writerConfig);
		
//		int numdocs = (this.writer.numDocs() == 0 ? index() : this.writer.numDocs());
		
//		logger.info("No of indexed docs: " + numdocs);
	}

	/*
	 * Build index over db.
	 */
	public int index() {

		// TODO das ist eigentlich VIIEL zu langsam!

		long start = System.currentTimeMillis();
		// retrieve pages
		PageRepository pageRepository = db.getPageRepository();
//		logger.info("Docs to index: " + pageRepository.count());
		Iterable<Page> allPages = pageRepository.findAll();
		System.out.println("pages retrieved");
		// add to index
		int count = 0;
		for (Page p : allPages) {
			indexPage(p);
			count++;
			System.out.println(count + ": " + p.toString());
			if (count > 10)// build temp index
				break;
		}
		logger.info("Indexing took " + (System.currentTimeMillis() - start) + " ms for " + count + " files.");
		return this.writer.numDocs();
	}

	private void indexPage(Page p) {
		Document doc = pageToLuceneDoc(p);
		if (doc != null) {
			try {
				this.writer.addDocument(doc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Convert Page to LuceneDoc.
	 */
	private Document pageToLuceneDoc(Page p) {
		Document doc = new Document();
		doc.add(new StringField("url", p.getUrl(), Store.YES));
		String pageId = p.getId();
		doc.add(new StringField("pageId", pageId, Store.YES));
		doc.add(new TextField("contents", pageContent(pageId), Store.YES));
		doc.add(new TextField("languages", languages(p), Store.YES));
		doc.add(new TextField("chapters", chapters(p), Store.YES));
		String volume = db.getVolumeRepository().findOne(p.getVolumeId()).getTitle();
		doc.add(new TextField("volume", volume, Store.YES));
		String ppn = p.getPrintedPageNuber();
		if (ppn != null)
			doc.add(new StringField("pageNumber", ppn, Store.YES));
		return doc;
	}

	/*
	 *  Retrieve chapter name(s) page belongs to.
	 */
	private String chapters(Page p) {
		StringBuilder sb = new StringBuilder();
		List<String> chapterIds = p.getChapterIds();
		for (String id : chapterIds) {
			sb.append(db.getChapterRepository().findOne(id) + " ");
		}
		return sb.toString().trim();
	}

	/*
	 *  Retrieve page language(s) from LanguageRepository.
	 */
	private String languages(Page p) {
		StringBuilder sb = new StringBuilder();
		List<String> languageIds = p.getLanguageIds();
		for (String id : languageIds) {
			sb.append(db.getLanguageRepository().findOne(id) + " ");
		}
		return sb.toString().trim();
	}

	/*
	 *  Retrieve page contents from WordRepository.
	 */
	private String pageContent(String pageId) {
		long start = System.currentTimeMillis();
		System.out.print("parse content ");
		StringBuilder sb = new StringBuilder();
		List<Word> words = db.getWordRepository().findByPageId(pageId);
		for (Word word : words) {
			sb.append(word.getCurrentVersion().getVersion() + " ");
		}
		logger.info("... took " + (System.currentTimeMillis() - start) + " ms.");
		return sb.toString().trim();
	}

	/*
	 * Gibt für collName eine Liste von JSON-Objekten als Strings zurück.
	 * [chapters, languages, page, pages, system.indexes, tokens, volumes, word,
	 * words, workingUnits]
	 */
	public List<String> getJSON(String collName) {
		return db.find(collName);
	}

	/*
	 * Convert page's json to LuceneDoc
	 */
	private Document jsonToLuceneDoc(String json) {

		// pages sehen dann so aus:
		/*
		 * { "_id" : { "$oid" : "5622c29eef86d3c2f23fd7db"} , "_class" :
		 * "de.uni_koeln.spinfo.drc.mongodb.data.document.Page" , "url" :
		 * "PPN345572629_0008-0220.xml" , "volumeId" :
		 * "5469dc1be4b0bc21df52eee4" , "chapterIds" : [
		 * "5622c2a0ef86d3c2f23fda0b"] , "languageIds" : [
		 * "5622c2a0ef86d3c2f23fd9a3"] , "start" : 186580 , "end" : 187155 ,
		 * "date" : { "$date" : "2014-06-04T13:10:27.557Z"} , "userId" : "admin"
		 * , "score" : 0}
		 */

		Document doc = new Document();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node;
		try {
			node = mapper.readTree(json);
			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				String value = node.get(fieldName).asText();
				doc.add(new TextField(fieldName, value, Store.YES));
				System.out.println(fieldName + " - " + value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.add(new TextField("contents", pageContent(doc.get("id")), Store.YES));
		String volume = db.getVolumeRepository().findOne(doc.get("volumeId")).getTitle();
		doc.add(new TextField("volume", volume, Store.YES));
		String languageIds = doc.get("languageIds");
		// doc.add(new TextField("languages", languages(p), Store.YES));
		String chapterIds = doc.get("chapterIds");
		// doc.add(new TextField("chapters", chapters(p), Store.YES));
		return doc;
	}

	/*
	 * Der Index steht erst zur Verfügung, wenn der Indexwriter geschlossen
	 * wurde.
	 */
	public void close() throws IOException {
		this.writer.close();
	}

}
