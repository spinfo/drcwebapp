package de.uni_koeln.spinfo.drc.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.spinfo.drc.lucene.Indexer;

@Controller
public class IndexerController {
	/*
	 * TODO Seite mit navbar + entsprechenden Aktionen.
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Indexer indexer;

	/*
	 * Indexierung manuell anstoßen.
	 */
	@RequestMapping("/reindex")
	@ResponseBody
	String reindex() {
		int numDocs = indexer.getNumDocs();
		try {
			if (numDocs > 0) {
				logger.info("Docs to be deleted: " + numDocs);
				indexer.init();
				indexer.deleteIndex();
			}
			long start = System.currentTimeMillis();
			indexer.init();
			logger.info("Indexing took " + (System.currentTimeMillis() - start) + " ms.");
			indexer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Re-index complete! New index size: " + indexer.getNumDocs() + "(was: " + numDocs + ").";
	}

	/*
	 * Index löschen.
	 */
	@RequestMapping("/deleteindex")
	@ResponseBody
	String deleteIndex() {
		int numDocs = indexer.getNumDocs();
		try {
			if (numDocs > 0) {
				logger.info("Docs to be deleted: " + numDocs);
				indexer.deleteIndex();
			} else {
				logger.info("Nothing to delete!");
				return "Nothing to delete!";
			}
			indexer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Index deleted!";
	}

}
