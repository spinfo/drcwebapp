package de.uni_koeln.spinfo.drc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.spinfo.drc.mongodb.Constants;
import de.uni_koeln.spinfo.drc.mongodb.DataBase;

@Controller()
@RequestMapping(value = "/drc")
public class VolumeController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DataBase db;
	

	@RequestMapping(value = "/volumes", method = RequestMethod.GET)
	public ModelAndView getVolumes() {
		
		ModelAndView mv = new ModelAndView(Constants.VOLUMES);
		mv.addObject("volumes", db.getVolumeRepository().findAll());
		
		return mv;
	}

}
