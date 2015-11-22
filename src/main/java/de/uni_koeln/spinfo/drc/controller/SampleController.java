package de.uni_koeln.spinfo.drc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SampleController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(value = "/samplePage")
	public ModelAndView mypage() {
		logger.info("calling samplePage.html");
		return new ModelAndView("samplePage");
	}

	@RequestMapping(value = "/samplePage2")
	public ModelAndView mypage2() {
		logger.info("calling samplePage2.html");
		return new ModelAndView("samplePage2");
	}

}
