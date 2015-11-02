package de.uni_koeln.spinfo.drc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	

	@RequestMapping(value = "/")
	public ModelAndView init() {
		return new ModelAndView("index");
	}

	@RequestMapping(value = "/about")
	public ModelAndView getInfo() {
		return new ModelAndView("about");
	}

	@RequestMapping(value = "/contact.html")
	public @ResponseBody
	ModelAndView getContacts() {
		ModelAndView mv = new ModelAndView("contact");
		return mv;
	}
	
}
