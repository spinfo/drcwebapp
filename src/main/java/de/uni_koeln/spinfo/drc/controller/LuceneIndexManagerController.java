package de.uni_koeln.spinfo.drc.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.spinfo.drc.lucene.Indexer;

@Controller
@RequestMapping(value = "/drc")
public class LuceneIndexManagerController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Indexer indexer;

	@RequestMapping(value = "/indexManager")
	public ModelAndView search() {
		return new ModelAndView("indexManager");
	}

	/*
	 * Trigger index manually - existing index will be deleted first.
	 */
	@RequestMapping("/reindex")
	@ResponseBody
	String reindex() {
		logger.info("RE-INDEX");
		int numDocsBefore = 0;
		int numDocsAfter = 0;
		try {
			if (!indexer.isAvailable()) {
				logger.info("Open indexWriter");
				indexer.init();
			}
			numDocsBefore = indexer.getNumDocs();
			if (numDocsBefore > 0) {
				logger.info("Docs to be deleted: " + numDocsBefore);
				indexer.deleteIndex();
			}
			indexer.index();
			numDocsAfter = indexer.getNumDocs();
			indexer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Re-index complete! New index size: " + numDocsAfter + "(was: " + numDocsBefore + ").";
	}

	/*
	 * Delete existing index.
	 */
	@RequestMapping("/deleteIndex")
	@ResponseBody
	String deleteIndex() {
		logger.info("DELETE INDEX");
		int numDocs = 0;
		try {
			if (!indexer.isAvailable()) {
				logger.info("Open indexWriter");
				indexer.init();
			}
			numDocs = indexer.getNumDocs();
			if (numDocs > 0) {
				indexer.deleteIndex();
			}
			indexer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return numDocs > 0 ? numDocs + " docs deleted!" : "Nothing to delete!";
	}
}