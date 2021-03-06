package com.pfizer.equip.searchservice.indexer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.api.client.ElasticSearchClient;
import com.pfizer.elasticsearch.api.client.ElasticSearchClientException;
import com.pfizer.equip.filedata.modeshape.NodeDataFetcher;
import com.pfizer.equip.lineage.dto.Analysis;
import com.pfizer.equip.lineage.dto.AnalysisAdapter;
import com.pfizer.equip.lineage.dto.Assembly;
import com.pfizer.equip.lineage.dto.AssemblyAdapter;
import com.pfizer.equip.lineage.dto.Batch;
import com.pfizer.equip.lineage.dto.BatchAdapter;
import com.pfizer.equip.lineage.dto.Dataframe;
import com.pfizer.equip.lineage.dto.DataframeAdapter;
import com.pfizer.equip.lineage.elasticsearch.LineageIndexer;
import com.pfizer.equip.searchable.dto.CommentData;
import com.pfizer.equip.searchable.dto.InstantDeserializer;
import com.pfizer.equip.searchable.dto.SearchableData;
import com.pfizer.equip.searchable.elasticsearch.CommentDataFetcher;
import com.pfizer.equip.searchable.elasticsearch.ReportingEventItemFetcher;
import com.pfizer.equip.searchable.elasticsearch.SearchableDataIndexer;
import com.pfizer.equip.searchable.elasticsearch.SearchableFetcher;
import com.pfizer.equip.searchable.exceptions.SearchableDataException;
import com.pfizer.equip.searchservice.AppPropertyNames;
import com.pfizer.equip.searchservice.resource.SearchLineageRoute;
import com.pfizer.equip.service.client.ServiceCallerException;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.modeshape.api.client.ModeshapeClientException;

/**
 * Indexer does the work of updating the search index when
 * new Searchable nodes are added or existing Searchable nodes 
 * are updated.
 * 
 * @author HeinemanWP
 *
 */
public class Indexer {
	private static Logger log = LoggerFactory.getLogger(Indexer.class);
	private static final String LAST_UPDATE_TIME_PROPERTY = "lastUpdateTime";
	private static final String LAST_UPDATE_TIME_PROPERTY_DEFAULT = "-1";
	private static final int UPDATE_COUNT = 100;
	private Properties appProperties;
	private String updateFilename;
	private Properties updateProperties = new Properties();
	private ModeshapeClient msc;
	private NodeDataFetcher ndf;
	private ElasticSearchClient esc;
	private SearchableFetcher sf;
	private CommentDataFetcher cdf;
	private SearchableDataIndexer searchableDataIndexer;
	private ReportingEventItemFetcher reportingEventItemFetcher;
	private LineageIndexer lineageIndexer;
	private Gson lineageAssemblyGson;
	private Gson lineageAnalysisGson;
	private Gson lineageBatchGson;
	private Gson lineageDataframeGson;
	
	public Indexer() {}
	
	public Indexer(Properties appProperties) {
		this.appProperties = appProperties;
		updateFilename = appProperties.getProperty(
				AppPropertyNames.INDEX_UPDATE_FILE, AppPropertyNames.INDEX_UPDATE_FILE_DEFAULT);
	}
	
	public void index() throws IndexerException {
		if (appProperties == null) {
			return;
		}
		// Get last update time from file
		long lastUpdateTime;
		try {
			lastUpdateTime = getLastUpdateTime();
			try {
				initializeServiceProviders();
	
				if (lastUpdateTime < 0L) {
					lastUpdateTime = 0L;
				}
				
				// Get new and updated searchables
				List<String> updatedIds = sf.getIdsUpdatedSince(Instant.ofEpochMilli(lastUpdateTime), UPDATE_COUNT);
				long lastModifiedTime = lastUpdateTime;
				// For each new and update searchable
				for (String updatedId : updatedIds) {
					try {
						lastModifiedTime = updateIndex(lastUpdateTime, updatedId);
					} catch (Exception ex) {
						lastModifiedTime += 1L;
						// Ignoring error and moving on.
						log.error("", ex);
					}
				}
				boolean reportingEventItemsUpdated = reportingEventItemFetcher.updateReportingEventItems(lastUpdateTime);
				
				if (lastModifiedTime <= Instant.now().toEpochMilli()) {
					lastUpdateTime = lastModifiedTime;
				}
				// Write new update time to file
				setLastUpdateTime(lastUpdateTime);

				if (!updatedIds.isEmpty() || reportingEventItemsUpdated) {
					searchableDataIndexer.refresh();
					lineageIndexer.refresh();
				}
				
			} catch(Exception ex) {
				throw new IndexerException("", ex);
			}
		} catch (IOException ex) {
			throw new IndexerException("getLastUpdateTime() failed", ex);
		}
	}

	public void indexForLineageChanges() throws IndexerException {
		if (appProperties == null) {
			return;
		}
		log.info("indexForLineageChanges()");
		try {
			initializeServiceProviders();

			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				// ignoring
			}
			// Set last update time to n seconds ago.
			long lastUpdateTime = Instant.now().toEpochMilli() - 10000L;
			if (lastUpdateTime < 0L) {
				lastUpdateTime = 0L;
			}
			
			// Get new and updated searchables
			List<String> updatedIds = sf.getIdsUpdatedSince(Instant.ofEpochMilli(lastUpdateTime), UPDATE_COUNT);
			// For each new and update searchable
			for (String updatedId : updatedIds) {
				updateLineageIndex(updatedId);
			}
			
			if (!updatedIds.isEmpty()) {
				lineageIndexer.refresh();
			}
			
		} catch(Exception ex) {
			throw new IndexerException("", ex);
		}
	}

	public void initializeServiceProviders() throws ServiceCallerException {
		if (msc == null) {
			log.info(String.format("appProperties.getProperty(AppPropertyNames.MODESHAPE_SERVER): %s", appProperties.getProperty(AppPropertyNames.MODESHAPE_SERVER)));
			msc = new ModeshapeClient(
					appProperties.getProperty(
							AppPropertyNames.MODESHAPE_SERVER),
					appProperties.getProperty(
							AppPropertyNames.MODESHAPE_USERNAME),
					appProperties.getProperty(
							AppPropertyNames.MODESHAPE_PASSWORD));
			ndf = new NodeDataFetcher(msc);
		}
		if (esc == null) {
			log.info(String.format("appProperties.getProperty(AppPropertyNames.ELASTICSEARCH_SERVER): %s", appProperties.getProperty(AppPropertyNames.ELASTICSEARCH_SERVER)));
			esc = new ElasticSearchClient(
					appProperties.getProperty(
							AppPropertyNames.ELASTICSEARCH_SERVER),
					appProperties.getProperty(
							AppPropertyNames.ELASTICSEARCH_USERNAME),
					appProperties.getProperty(
							AppPropertyNames.ELASTICSEARCH_PASSWORD));
			sf = new SearchableFetcher(esc);
			cdf = new CommentDataFetcher(esc);
			searchableDataIndexer = new SearchableDataIndexer(esc);
			lineageIndexer = new LineageIndexer(esc);
			reportingEventItemFetcher = new ReportingEventItemFetcher(esc, msc);
			lineageAssemblyGson = new GsonBuilder()
					.registerTypeAdapter(Instant.class, new InstantDeserializer())
					.registerTypeAdapter(Assembly.class, new AssemblyAdapter(msc))
					.create();
			lineageAnalysisGson = new GsonBuilder()
					.registerTypeAdapter(Instant.class, new InstantDeserializer())
					.registerTypeAdapter(Analysis.class, new AnalysisAdapter(msc))
					.create();
			lineageBatchGson = new GsonBuilder()
					.registerTypeAdapter(Instant.class, new InstantDeserializer())
					.registerTypeAdapter(Batch.class, new BatchAdapter(msc))
					.create();
			lineageDataframeGson = new GsonBuilder()
					.registerTypeAdapter(Instant.class, new InstantDeserializer())
					.registerTypeAdapter(Dataframe.class, new DataframeAdapter(msc))
					.create();
		}
	}

	public long updateIndex(long lastUpdateTime, String updatedId) {
		try {
			// Update or insert index
			// String logMsg = String.format("P: %s", updatedId); 
			// log.info(logMsg);
			SearchableData parentNodeData = ndf.fetchNode(updatedId);
			try {
				SearchableData contentData = null;
				try {
					contentData = ndf.fetchContentNode(updatedId);
				} catch (SearchableDataException ex) {
					log.warn("", ex);
				}
				boolean noContentData = contentData == null;
				if (noContentData) {
					contentData = parentNodeData;
				}
				List<CommentData> comments = cdf.getDataByPath(parentNodeData.getJcrPath(), 0, 1000);
				String contentId;
				if (noContentData) {
					contentData.setEquipCommentFromCommentData(comments);
					contentId = searchableDataIndexer.insertContentNode(updatedId, contentData);
				} else {
					contentId = contentData.getJcrUuid();
					SearchableData searchableData = ndf.fetchSearchableData(contentId, parentNodeData);
					if ((searchableData.getJcrMimeType() != null)
							&& searchableData.getJcrMimeType().startsWith("text/csv")) {
						contentData.setJcrMimeType("text/csv");
						ndf.updateNodeMimeType(contentData.getFileId(), "text/csv");
					}
					searchableData.setEquipCommentFromCommentData(comments);
					try {
						searchableDataIndexer.updateContentNode(contentId, searchableData);
					} catch(ElasticSearchClientException ex) {
						log.warn("", ex);
						searchableDataIndexer.updateContentNode(contentId, parentNodeData);
					}
				}
				
				lastUpdateTime = updateLastUpdateTime(lastUpdateTime, parentNodeData);
			} finally {
				updateLineageIndex(lineageIndexer, parentNodeData);
			}
		} catch(Exception ex) {
			log.error("", ex);
		}
		return lastUpdateTime;
	}

	private void updateLineageIndex(String updatedId) {
		try {
			SearchableData parentNodeData = ndf.fetchNode(updatedId);
			updateLineageIndex(lineageIndexer, parentNodeData);
		} catch(Exception ex) {
			log.error("", ex);
		}
	}
	
	private void updateLineageIndex(LineageIndexer lineageIndexer, SearchableData parentNodeData) throws ModeshapeClientException, ElasticSearchClientException {
		String jcrPrimaryType = parentNodeData.getJcrPrimaryType();
		if (jcrPrimaryType.equals("equip:assembly")
				|| jcrPrimaryType.equals("equip:analysis")
				|| jcrPrimaryType.equals("equip:batch")
				|| jcrPrimaryType.equals("equip:dataframe")) {
			String uuid = parentNodeData.getJcrUuid();
			String json = msc.retrieveNodeById("equip", "nca", uuid);
			Object obj = null;
			if (jcrPrimaryType.equals("equip:assembly")) {
				obj = lineageAssemblyGson.fromJson(json, Assembly.class);
			} else if (jcrPrimaryType.equals("equip:analysis")) {
				obj = lineageAnalysisGson.fromJson(json, Analysis.class);
			} else if (jcrPrimaryType.equals("equip:batch")) {
				obj = lineageBatchGson.fromJson(json, Batch.class);
			} else {
				obj = lineageDataframeGson.fromJson(json, Dataframe.class);
			}
			lineageIndexer.insertNode(uuid, obj);
		}
	}

	private long updateLastUpdateTime(long lastUpdateTime, SearchableData parentNodeData) {
		Instant parentCreated = parentNodeData.getJcrCreated();
		Instant parentModified = parentNodeData.getJcrLastModified();
		Instant parentUpdated = parentCreated;
		if (parentModified != null) {
			parentUpdated = parentModified;
		}
		if (parentUpdated.toEpochMilli() > lastUpdateTime) {
			lastUpdateTime = parentUpdated.toEpochMilli();
		}
		return lastUpdateTime;
	}
	
	private long getLastUpdateTime() throws IOException {
		try(FileReader fr = new FileReader(updateFilename) ){
			updateProperties.load(fr);
			long returnValue = Long.parseLong(
					updateProperties.getProperty(LAST_UPDATE_TIME_PROPERTY, LAST_UPDATE_TIME_PROPERTY_DEFAULT));
			if (returnValue < 0) {
				returnValue = Instant.now().toEpochMilli();
			}
			return returnValue;
		} catch (IOException ex) {
			log.error(String.format("Failed to load update properties file %s", 
					updateFilename), ex);
			throw ex;
		}
	}
	
	private void setLastUpdateTime(long lastUpdateTime) {
		try(FileWriter fw = new FileWriter(updateFilename)) {
			updateProperties.put(LAST_UPDATE_TIME_PROPERTY, Long.toString(lastUpdateTime));
			Instant instant = Instant.ofEpochMilli(lastUpdateTime);
			updateProperties.store(fw, 
					String.format("%s\n", instant.toString()));
		} catch (IOException ex) {
			log.error(String.format("Failed to save update properties file %s", 
					updateFilename), ex);
		}
	}

	public void setProperties(Properties appProperties) {
		msc = null;
		esc = null;
		this.appProperties = appProperties;
		updateFilename = appProperties.getProperty(
				AppPropertyNames.INDEX_UPDATE_FILE, AppPropertyNames.INDEX_UPDATE_FILE_DEFAULT);
	}
	
}
