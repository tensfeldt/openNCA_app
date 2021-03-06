package com.pfizer.equip.searchservice.indexer;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages thread that does the indexing.
 * 
 * @author HeinemanWP
 *
 */
public class IndexerRunner {
	private static Logger log = LoggerFactory.getLogger(IndexerRunner.class);
	private ExecutorService service;
	private boolean running = false;
	private Indexer indexer = new Indexer();
	private final int indexingSleepTime;

	public IndexerRunner(int indexingSleepTime) {
		this.indexingSleepTime = indexingSleepTime;
		service = Executors.newSingleThreadExecutor();
	}

	public void start() {
		if (!running) {
			service.execute(() -> {
				while (true) {
					process();
				}
			});
			running = true;
		}
	}
	
	public void stop() {
		if (!service.isShutdown()) {
			service.shutdown();
		}
		running = false;
	}
	
	private void process() {
		try {
			indexer.index();
		} catch (IndexerException ex) {
			log.error("", ex);
		}
		try {
			Thread.sleep(indexingSleepTime);
		} catch (InterruptedException ex) {
			log.error("", ex);
		}
	}

	public void setProperties(Properties appProperties) {
		indexer.setProperties(appProperties);
	}
}
