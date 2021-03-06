package com.pfizer.equip.searchservice.indexer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.searchservice.AppPropertyNames;

/**
 * Servlet implementation class IndexerServlet
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/IndexerServlet" })
public class IndexerServlet extends HttpServlet {
	private static Logger log = LoggerFactory.getLogger(IndexerServlet.class);	
	private static final long serialVersionUID = 7504136749954654589L;
	private static final String APPLICATION_PROPERTIES_FILE = "/app/3rdparty/equip/EquipSearchService/application.properties";
	private IndexerRunner indexerRunner;

    /**
     * Default constructor. 
     */
    public IndexerServlet() {
    	super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
    @Override
	public void init(ServletConfig config) throws ServletException {
    	log.info("IndexerServlet::init");
    	super.init(config);
		Properties appProperties = new Properties();
		// Load application properties
		try (FileReader fr = new FileReader(APPLICATION_PROPERTIES_FILE)) {
			appProperties.load(fr);
			log.info(String.format("appProperties.getProperty(AppPropertyNames.MODESHAPE_SERVER): %s", appProperties.getProperty(AppPropertyNames.MODESHAPE_SERVER)));
			log.info(String.format("appProperties.getProperty(AppPropertyNames.ELASTICSEARCH_SERVER): %s", appProperties.getProperty(AppPropertyNames.ELASTICSEARCH_SERVER)));
		} catch (IOException ex) {
			log.error(String.format("Failed to load application properties file %s", 
					APPLICATION_PROPERTIES_FILE), ex);
		}
		// If indexing is enabled for this instance, start indexing
		boolean indexingEnabled = Boolean.parseBoolean(appProperties.getProperty(
				AppPropertyNames.INDEXING_ENABLED, AppPropertyNames.INDEXING_ENABLED_DEFAULT));
		int indexingSleepTime = Integer.parseInt(appProperties.getProperty(
				AppPropertyNames.INDEXING_SLEEP_TIME, AppPropertyNames.INDEXING_SLEEP_TIME_DEFAULT));
		log.info(String.format("indexingEnabled: %s, indexingSleepTime: %d", indexingEnabled, indexingSleepTime));
		if (indexingEnabled) {
			indexerRunner = new IndexerRunner(indexingSleepTime);
			indexerRunner.setProperties(appProperties);
			indexerRunner.start();
		}
	}

	@Override
	public void destroy() {
		indexerRunner.stop();
		super.destroy();
	}

}
