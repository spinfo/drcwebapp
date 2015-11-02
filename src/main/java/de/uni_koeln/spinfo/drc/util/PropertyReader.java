package de.uni_koeln.spinfo.drc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class PropertyReader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Properties properties;

	private ResourceLoader resourceLoader;
	
	private static final String INDEX = "lucene.index";

	@Inject
	public PropertyReader(ResourceLoader resourceLoader) throws IOException {
		this.resourceLoader = resourceLoader;
		this.properties = new Properties();
		init();
	}

	public void init() throws IOException {
		loadProperties(resourceLoader.getResource("application.properties").getFile());
	}

	private void loadProperties(File file) throws IOException {
		logger.info("Loading properties from file : " + file.getAbsolutePath());
		FileInputStream fileInputStream = new FileInputStream(file);
		this.properties.load(fileInputStream);
		fileInputStream.close();
	}

	public String getIndexDir() {
		return properties.getProperty(INDEX);
	}

}
