package de.uni_koeln.spinfo.drc.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

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

import de.uni_koeln.spinfo.drc.util.PropertyReader;

@Service
public class Indexer {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	PropertyReader propertyReader;

	private IndexWriter writer;

	public Indexer() {
	}

	public void init(final String indexDir) throws IOException, URISyntaxException {
		Directory dir = new SimpleFSDirectory(Paths.get(new URI(indexDir)));
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
		this.writer = new IndexWriter(dir, writerConfig);
	}

	public int index(String source) throws Exception {
		File dataDir = new File(source);
		dataDir.mkdirs();

		File[] subDirs = dataDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});

		logger.info(dataDir.getName() + " > DIRECTORY.COUNT: " + subDirs.length);
		for (File dir : subDirs) {
			dir.mkdirs(); 
			File[] files = dir.listFiles();
			logger.info(dir.getName() + " > FILE.COUNT: " + files.length);
			FileFilter filter = getFileFilter();
			for (File f : files) {
				if (filter.accept(f)) {
					indexFile(f);
				}
			}
		}
		return this.writer.numDocs();
	}

	private FileFilter getFileFilter() {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return !f.isDirectory() && !f.isHidden() && f.exists()
						&& f.canRead() && f.getName().endsWith(".xml");
			}
		};
		return filter;
	}

	private Document getDocument(File f) throws Exception {
		Document doc = new Document();
		BufferedReader br = new BufferedReader(new FileReader(f));

		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		
		doc.add(new TextField("contents", sb.toString(), Store.YES));
		doc.add(new StringField("filename", f.getName(), Store.YES));

		return doc;
	}

	private void indexFile(File f) throws Exception {
		Document doc = getDocument(f);
		if (doc != null) {
			this.writer.addDocument(doc);
		}
	}

	public void close() throws IOException {
		this.writer.close();
	}

}
