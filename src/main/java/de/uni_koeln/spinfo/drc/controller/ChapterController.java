package de.uni_koeln.spinfo.drc.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.spinfo.drc.mongodb.Constants;
import de.uni_koeln.spinfo.drc.mongodb.DataBase;
import de.uni_koeln.spinfo.drc.mongodb.data.document.Chapter;
import de.uni_koeln.spinfo.drc.mongodb.data.document.Volume;

@Controller()
@RequestMapping(value = "/drc")
public class ChapterController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DataBase db;

	@RequestMapping(value = "/chapters")
	public ModelAndView getChapters(@RequestParam("volumeId") String volumeId) {
		
		Volume volume = db.getVolumeRepository().findOne(volumeId);
		List<Chapter> chapters = db.getChapterRepository().findByVolumeId(volume.getId());
		
		ModelAndView mv = new ModelAndView(Constants.CHAPTERS);
		mv.addObject("chapters", chapters);
		mv.addObject("volume", volume);
		
		return mv;
	}

}
