package de.uni_koeln.spinfo.drc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(value = "/mypage")
	public ModelAndView mypage() {
		logger.info("calling mypage.html");
		return new ModelAndView("mypage");
	}

	@RequestMapping(value = "/mypage2")
	public ModelAndView mypage2() {
		logger.info("calling mypage2.html");
		return new ModelAndView("mypage2");
	}

}
