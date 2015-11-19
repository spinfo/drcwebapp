package de.uni_koeln.spinfo.drc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.uni_koeln.spinfo.drc.mongodb.DataBase;
import de.uni_koeln.spinfo.drc.mongodb.data.document.Page;
import de.uni_koeln.spinfo.drc.mongodb.data.document.Volume;
import de.uni_koeln.spinfo.drc.mongodb.data.document.Word;
import de.uni_koeln.spinfo.drc.mongodb.repository.PageRepository;
import de.uni_koeln.spinfo.drc.util.PropertyReader;

@Service
public class Indexer {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DataBase db;

	@Autowired
	PropertyReader propertyReader;

	private IndexWriter writer;

	private static final int INDEXSIZE = 100;

	// public Indexer() {
	// }

	@PostConstruct // wird aufgerufen, sobald alle Services bereit sind
	public void init() throws IOException {
		String indexDir = propertyReader.getIndexDir();
		Directory dir = new SimpleFSDirectory(new File(indexDir).toPath());
		IndexWriterConfig writerConfig = new IndexWriterConfig(new StandardAnalyzer());
		this.writer = new IndexWriter(dir, writerConfig);
		// Wenn noch kein Index vorliegt, erstellen:
		if (this.writer.numDocs() == 0) {
			logger.info("Indexing collection...");
			int indexSize = index(INDEXSIZE);
			logger.info("Index size: " + indexSize);
			this.writer.close();
		}
	}

	/*
	 * Build index over db, restricted to a predefined size.
	 */
	public int index(int size) {

		long start = System.currentTimeMillis();

		logger.info("Docs to index: " + db.getMongoTemplate().count(new Query(), Page.class));

		Iterable<Volume> volumes = db.getVolumeRepository().findAll();
		int pageCount = 0;
		boolean escape = false;
		for (Volume volume : volumes) {
			List<Page> pages = db.getPageRepository().findByVolumeId(volume.getId());
			for (Page p : pages) {

				// Iterable<Page> allPages = pageRepository.findAll();
				// logger.info("All pages retrieved from db repo.");
				// List<Page> allPages = new ArrayList<Page>();
				// allPages.add(pageRepository.findByUrl("PPN345572629_0014_02-0007.xml"));
				// allPages.add(pageRepository.findByUrl("PPN345572629_0014_02-0008.xml"));
				// allPages.add(pageRepository.findByUrl("PPN345572629_0014_02-0009.xml"));
				// allPages.add(pageRepository.findByUrl("PPN345572629_0014_02-0010.xml"));

				pageCount++;
				if (pageCount <= size) {
					logger.info(pageCount + ": " + p.toString());
					indexPage(p);
				}else{
					escape = true;
					break;
				}
				
			}
			if(escape)
				break;
			
		}
		logger.info("Indexing took " + (System.currentTimeMillis() - start) + " ms for " + this.writer.numDocs()
				+ " pages.");
		return this.writer.numDocs();
	}

	private void indexPage(Page p) {
		Document doc = pageToLuceneDoc(p);
		try {
			if (doc != null) {
				logger.info("Adding doc: " + p.toString());
				this.writer.addDocument(doc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Convert page info in Lucene Document.
	 */
	private Document pageToLuceneDoc(Page p) {
		Document doc = new Document();
		doc.add(new StringField("url", p.getUrl(), Store.YES));
		String pageId = p.getId();
		doc.add(new StringField("pageId", pageId, Store.YES));
		doc.add(new TextField("contents", pageContent(p), Store.YES));
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
	 * Retrieve chapter name(s) a page belongs to.
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
	 * Retrieve page language(s) from LanguageRepository.
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
	 * Retrieve page contents from WordRepository.
	 */
	private String pageContent(Page page) {
		long start = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		List<Word> words = db.getWordRepository().findByRange(page.getStart(), page.getEnd());
		for (Word word : words) {
			sb.append(word.getCurrentVersion().getVersion() + " ");
		}
		logger.info("... parse took " + (System.currentTimeMillis() - start) + " ms.");
		return sb.toString().trim();
	}

	/*
	 * Der Index steht erst dann für die Suche zur Verfügung, wenn der Writer
	 * geschlossen wurde!
	 */
	public void close() throws IOException {
		this.writer.close();
	}

	/*
	 * Hilfsmethode für Re-Indexierung u.ä.
	 */
	public int getNumDocs() {
		return writer.numDocs();
	}

	/*
	 * Hilfsmethode für Re-Indexierung u.ä.
	 */
	public void deleteIndex() {
		try {
			this.writer.deleteAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Gibt für collName eine Liste von JSON-Objekten als Strings zurück.
	 * [chapters, languages, page, pages, system.indexes, tokens, volumes, word,
	 * words, workingUnits]
	 */
	public List<String> getJSON(String collName) {
		//return db.getMongoTemplate().find(new Query(), String.class, collName);
		return db.find(collName);
	}

}
