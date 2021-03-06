package com.pfizer.equip.searchservice.indexer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.pfizer.equip.lineage.dto.Dataframe;
import com.pfizer.equip.lineage.dto.DataframeAdapter;
import com.pfizer.equip.lineage.elasticsearch.LineageIndexer;
import com.pfizer.equip.searchable.dto.CommentData;
import com.pfizer.equip.searchable.dto.InstantDeserializer;
import com.pfizer.equip.searchable.dto.SearchableData;
import com.pfizer.equip.searchable.elasticsearch.CommentDataFetcher;
import com.pfizer.equip.searchable.elasticsearch.SearchableDataIndexer;
import com.pfizer.equip.searchable.elasticsearch.SearchableFetcher;
import com.pfizer.equip.searchservice.AppPropertyNames;
import com.pfizer.equip.service.client.ServiceCallerException;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.modeshape.api.client.ModeshapeClientException;

public class NovaIndexer {
	private static Logger log = LoggerFactory.getLogger(Indexer.class);
	private static final String LAST_UPDATE_TIME_PROPERTY = "lastUpdateTime";
	private static final String LAST_UPDATE_TIME_PROPERTY_DEFAULT = "-1";
	private static final int UPDATE_COUNT = 100;
	private Properties appProperties;
	private Map<String, String> indexedCache = new HashMap<>();
	private String updateFilename;
	private Properties updateProperties = new Properties();
	private ModeshapeClient msc;
	private ElasticSearchClient esc;
	private NodeDataFetcher ndf;
	private SearchableFetcher sf;
	private CommentDataFetcher cdf;
	private SearchableDataIndexer searchableDataIndexer;
	private LineageIndexer lineageIndexer;
	private Gson lineageAssemblyGson;
	private Gson lineageAnalysisGson;
	private Gson lineageDataframeGson;

	
	public NovaIndexer() {}
	
	public NovaIndexer(Properties appProperties) {
		this.appProperties = appProperties;
		updateFilename = appProperties.getProperty(
				AppPropertyNames.INDEX_UPDATE_FILE, AppPropertyNames.INDEX_UPDATE_FILE_DEFAULT);
	}
	
	public void index() throws IndexerException {
		if (appProperties == null) {
			return;
		}
		try {
			initializeServiceProviders();

			// Get last update time from file
			long lastUpdateTime = getLastUpdateTime();
			
			// Get new and updated searchables
			List<String> updatedIds = sf.getIdsUpdatedSince(Instant.ofEpochMilli(lastUpdateTime), UPDATE_COUNT);
			// Remove ids that have alreadly been indexed.
			
			// For each new and update searchable
			for (String updatedId : updatedIds) {
				long lastModifiedTime = updateIndex(lastUpdateTime, updatedId);
				if (lastModifiedTime <= Instant.now().toEpochMilli()) {
					lastUpdateTime = lastModifiedTime;
				}
			}
			if (!updatedIds.isEmpty()) {
				searchableDataIndexer.refresh();
				lineageIndexer.refresh();
			}
			// Write new update time to file
			setLastUpdateTime(lastUpdateTime);
		} catch(Exception ex) {
			throw new IndexerException("", ex);
		}
	}

	private void initializeServiceProviders() throws ServiceCallerException {
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
			lineageAssemblyGson = new GsonBuilder()
					.registerTypeAdapter(Instant.class, new InstantDeserializer())
					.registerTypeAdapter(Assembly.class, new AssemblyAdapter(msc))
					.create();
			lineageAnalysisGson = new GsonBuilder()
					.registerTypeAdapter(Instant.class, new InstantDeserializer())
					.registerTypeAdapter(Analysis.class, new AnalysisAdapter(msc))
					.create();
			lineageDataframeGson = new GsonBuilder()
					.registerTypeAdapter(Instant.class, new InstantDeserializer())
					.registerTypeAdapter(Dataframe.class, new DataframeAdapter(msc))
					.create();
		}
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
	
	private long updateIndex(long lastUpdateTime, String updatedId) {
		try {
			// Update or insert index
			// String logMsg = String.format("P: %s", updatedId); 
			// log.info(logMsg);
			SearchableData parentNodeData = ndf.fetchNode(updatedId);
			SearchableData contentData = ndf.fetchContentNode(updatedId);
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
						&& searchableData.getJcrMimeType().equalsIgnoreCase("text/csv")) {
					contentData.setJcrMimeType("text/csv");
				}
				searchableData.setEquipCommentFromCommentData(comments);
				searchableDataIndexer.updateContentNode(contentId, searchableData);
			}
			if ((contentData.getJcrMimeType() != null)
					&& contentData.getJcrMimeType().equalsIgnoreCase("text/csv")) {
				ndf.updateNodeMimeType(contentData.getFileId(), "text/csv");
			}
			updateLineageIndex(lineageIndexer, parentNodeData);
			lastUpdateTime = updateLastUpdateTime(lastUpdateTime, parentNodeData);
			// logMsg = String.format("C: %s", contentId);
			// log.info(logMsg);
		} catch(Exception ex) {
			log.error("", ex);
		}
		return lastUpdateTime;
	}

	private void updateLineageIndex(LineageIndexer lineageIndexer, SearchableData parentNodeData) throws ModeshapeClientException, ElasticSearchClientException {
		String jcrPrimaryType = parentNodeData.getJcrPrimaryType();
		if (jcrPrimaryType.equals("equip:assembly")
				|| jcrPrimaryType.equals("equip:analysis")
				|| jcrPrimaryType.equals("equip:dataframe")) {
			String uuid = parentNodeData.getJcrUuid();
			String json = msc.retrieveNodeById("equip", "nca", uuid);
			Object obj = null;
			if (jcrPrimaryType.equals("equip:assembly")) {
				obj = lineageAssemblyGson.fromJson(json, Assembly.class);
			} else if (jcrPrimaryType.equals("equip:analysis")) {
				obj = lineageAnalysisGson.fromJson(json, Analysis.class);
			} else {
				obj = lineageDataframeGson.fromJson(json, Dataframe.class);
			}
			lineageIndexer.insertNode(uuid, obj);
		}
	}
	
} 
