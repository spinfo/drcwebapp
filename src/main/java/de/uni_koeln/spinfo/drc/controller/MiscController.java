package de.uni_koeln.spinfo.drc.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.spinfo.drc.lucene.ExtendedSearcher;
import de.uni_koeln.spinfo.drc.lucene.SearchResult;
import de.uni_koeln.spinfo.drc.mongodb.DataBase;

@Controller()
@RequestMapping(value = "/misc")
public class MiscController {

	/*
	 * TODO @Miha wieso wird reindex schon getriggert, wenn ich nur den ersten
	 * buchstaben eingebe (ohne return?)
	 * 
	 * @Miha: muss der Link zur page-anzeige alle Infos enthalten? Das ist
	 * relevant z.B. auch für die Einbettung des Suchergebnis: das kann nur im
	 * Tab dargestellt werden, wenn die page-params 'durchgereicht' werden ...
	 * 
	 */

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ExtendedSearcher extendedSearcher;

	@Autowired
	private DataBase db;

	/**
	 * The search page
	 */
	@RequestMapping(value = "/search")
	public ModelAndView search() {
		return new ModelAndView("search");
	}

	/*
	 * Extended result view.
	 */
	@RequestMapping(value = "/searchResult")
	public ModelAndView displayResults(@RequestParam("search") String searchPhrase,
			@RequestParam(value = "resultPage", defaultValue = "1") int resultPageNo) {

		logger.info("search=" + searchPhrase);
		logger.info("resultPage=" + resultPageNo);// für Paginierung

		List<SearchResult> resultList = null;
		int hitsPerPage = 10; // als testParam (selection hitsperpage)
		int offset = (resultPageNo - 1) * hitsPerPage;
		int totalHits = 0;

		try {
			resultList = extendedSearcher.search(searchPhrase, offset);
			totalHits = extendedSearcher.getTotalHits();
		} catch (ParseException | InvalidTokenOffsetsException | URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		ModelAndView mv = new ModelAndView("searchResult");
		mv.addObject("searchPhrase", searchPhrase);
		mv.addObject("hits", resultList);
		mv.addObject("totalHits", totalHits);
		mv.addObject("offset", offset);
		mv.addObject("resultPage", resultPageNo);
		logger.info("offset: "+offset);
		return mv;
	}

	/*
	 * try using list of Documents ...
	 * 
	 * @RequestMapping(value = "/searchResult") public ModelAndView
	 * simpleResult2(@RequestParam("search") String searchPhrase) {
	 * logger.info("search=" + searchPhrase); List<Document> resultList = null;
	 * int totalHits = 0; try { resultList =
	 * searcher.basicSearch2(propertyReader.getIndexDir(), searchPhrase);
	 * totalHits = searcher.getTotalHits(); } catch (ParseException |
	 * IOException e) { e.printStackTrace(); }
	 * 
	 * ModelAndView mv = new ModelAndView("searchResult");
	 * mv.addObject("searchPhrase", searchPhrase); mv.addObject("hits",
	 * resultList); mv.addObject("totalHits", totalHits); return mv; }
	 */

	/*
	 * JSON direkt in map ablegen -- "shortcut-variante", siehe
	 * http://stackoverflow.com/questions/2525042/how-to-convert-a-json-string-
	 * to-a-mapstring-string-with-jackson-json
	 * 
	 */
	@SuppressWarnings("unchecked") // für ObjectMapper
	@RequestMapping("/jsonMap")
	@ResponseBody
	String mapJSON() {

		long start = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		List<String> json = getJSON("volumes");

		HashMap<String, String> props;
		try {
			for (String string : json) {
				props = new ObjectMapper().readValue(string, HashMap.class);

				logger.info(props.entrySet() + "");

				sb.append(props.entrySet() + "<br/> <br/>");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("db query took " + (System.currentTimeMillis() - start) + " ms.");
		return sb.toString();
	}

	/*
	 * JSON parsen.
	 */
	@RequestMapping("/jsonParse")
	@ResponseBody
	String parseJSON() {

		long start = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		List<String> json = getJSON("volumes");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node;
		try {
			for (String string : json) {
				node = mapper.readTree(string);
				Iterator<String> fieldNames = node.fieldNames();
				while (fieldNames.hasNext()) {
					String fieldName = fieldNames.next();
					String fieldContent = node.get(fieldName).toString();
					sb.append(fieldName + ":" + fieldContent + ", ");

					logger.info(fieldName + " - " + node.get(fieldName));
				}
				sb.append("<br/><br/>");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("db query took " + (System.currentTimeMillis() - start) + " ms.");
		return sb.toString();
	}

	/*
	 * Gibt für collName eine Liste von JSON-Objekten als Strings zurück.
	 * [chapters, languages, page, pages, system.indexes, tokens, volumes, word,
	 * words, workingUnits]
	 */
	public List<String> getJSON(String collName) {
		return db.find(collName);
	}

}
