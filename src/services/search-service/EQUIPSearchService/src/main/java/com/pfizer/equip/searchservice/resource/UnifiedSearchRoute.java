package com.pfizer.equip.searchservice.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.elasticsearch.dto.Predicate;
import com.pfizer.elasticsearch.dto.PredicateAdapter;
import com.pfizer.elasticsearch.dto.PropertyValuePair;
import com.pfizer.elasticsearch.dto.PropertyValuePairAdapter;
import com.pfizer.elasticsearch.dto.Query;
import com.pfizer.equip.searchservice.dto.FileDataSearchRequest;
import com.pfizer.equip.searchservice.dto.FileDataSearchRequestAdapter;
import com.pfizer.equip.searchservice.dto.MetaDataSearchRequest;
import com.pfizer.equip.searchservice.dto.MetaDataSearchRequestAdapter;
import com.pfizer.equip.searchservice.dto.SearchResponse;
import com.pfizer.equip.searchservice.dto.UnifiedSearchRequest;
import com.pfizer.equip.searchservice.dto.UnifiedSearchRequestAdapter;
import com.pfizer.equip.searchservice.exception.SearchException;
import com.pfizer.equip.searchservice.search.Search;
import com.pfizer.equip.searchservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.authorization.client.AuthorizationServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Implements SparkJava Route for unified searches
 * 
 * @author HeinemanWP
 *
 */
public class UnifiedSearchRoute implements Route {
	private static Logger log = LoggerFactory.getLogger(UnifiedSearchRoute.class);
	private AuthorizationServiceClient authorizationServiceClient;

	@Override
	public Object handle(Request request, Response response) throws Exception {
		log.info(request.body());
		if (authorizationServiceClient == null) {
			authorizationServiceClient = new AuthorizationServiceClient(
					ResourceCommon.AUTH_SERVER,
					Integer.parseInt(ResourceCommon.AUTH_PORT),
					ResourceCommon.AUTH_SYSTEM);
		}
		String userId = request.headers(ResourceCommon.IAMPFIZERUSERCN);
		if (userId == null) {
			Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
		}
		String idsOnlyString = request.queryParams("idsOnly");
		boolean idsOnly = false;
		if ((idsOnlyString != null) && !idsOnlyString.isEmpty()) {
			idsOnly = Boolean.parseBoolean(idsOnlyString);
		}		
		UnifiedSearchRequest usRequest = unmarshallSearchUnifiedRequest(request);
		SearchResponse searchResponse = initiateUnifiedSearches(userId, usRequest, idsOnly);
		response.header("Content-Type", "application/json");
		response.body((String) SearchResource.marshallSearchResponse(searchResponse));
		return response;
	}

	private UnifiedSearchRequest unmarshallSearchUnifiedRequest(Request request) {
		Gson gson = getGson();
		return gson.fromJson(request.body(), UnifiedSearchRequest.class);
	}

	private SearchResponse initiateUnifiedSearches(String userId, UnifiedSearchRequest usRequest, boolean idsOnly) throws SearchException {
		String[] sourcesIncluded = {"jcr:uuid", "jcr:primaryType"};
		String[] sourcesExcluded = {};
		if (!idsOnly) {
			sourcesIncluded = ResourceCommon.UNIFIED_SEARCH_SOURCES_INCLUDE;
			sourcesExcluded = ResourceCommon.UNIFIED_SEARCH_SOURCES_EXCLUDE;
		}
		Predicate predicate = usRequest.toElasticSearch();
		Query query = new Query(sourcesIncluded, sourcesExcluded, predicate);
		Search search = new Search(authorizationServiceClient);
		return search.initiateSearch(
				userId,
				ResourceCommon.ELASTICSEARCH_SERVER,
				ResourceCommon.ELASTICSEARCH_USERNAME,
				ResourceCommon.ELASTICSEARCH_PASSWORD,
				ResourceCommon.FILETEXT_SEARCH_INDEX,
				ResourceCommon.SEARCH_TYPE,
				query);
	}

	private Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(UnifiedSearchRequest.class, new UnifiedSearchRequestAdapter());
		gsonBuilder.registerTypeAdapter(MetaDataSearchRequest.class, new MetaDataSearchRequestAdapter());
		gsonBuilder.registerTypeAdapter(FileDataSearchRequest.class, new FileDataSearchRequestAdapter());
		gsonBuilder.registerTypeAdapter(PropertyValuePair.class, new PropertyValuePairAdapter());
		gsonBuilder.registerTypeAdapter(Predicate.class, new PredicateAdapter());
		return gsonBuilder.create();
	}

}
