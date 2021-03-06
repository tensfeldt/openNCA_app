package com.pfizer.equip.searchservice.resource;

import com.google.gson.Gson;
import com.pfizer.equip.searchservice.AppPropertyNames;
import com.pfizer.equip.searchservice.Application;
import com.pfizer.equip.searchservice.dto.SearchResponse;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Aggregator for all implemented SparkJava Route instances.
 * 
 * @author HeinemanWP
 *
 */
public class SearchResource {
	private SearchResource() {}
	
	public static final Route getVersion = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			return Application.getAppProperties().getProperty(AppPropertyNames.VERSION);
		}
	};

	public static final Route postSearchMetaData = new MetaDataSearchRoute();
	
	public static final Route postSearchComments = new CommentsSearchRoute();

	public static final Route postSearchFileData = new FileDataSearchRoute();

	public static final Route postSearchFileText = new FileTextSearchRoute();

	public static final Route postSearchUnified = new UnifiedSearchRoute();

	public static final Route getSearchResults = new SearchResultsRoute();
	
	public static final Route getSearchLineage = new SearchLineageRoute();
	
	protected static Object marshallSearchResponse(SearchResponse searchResponse) {
		Gson gson = new Gson();
		return gson.toJson(searchResponse);
	}

}
