package com.pfizer.elasticsearch.dto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for reading and writing Predicate JSON 
 * instances of search query.
 * 
 * @author HeinemanWP
 *
 */
public class PredicateAdapter extends TypeAdapter<Predicate> {
	private static String[] exactMatchValues = {
			"equip:dataStatus",
			"equip:promotionStatus",
			"equip:publishStatus",
			"equip:releaseStatus"
	};
	private static List<String> exactMatchValuesList = Arrays.asList(exactMatchValues);

	@Override
	public Predicate read(JsonReader in) throws IOException {
		return null;
	}

	@Override
	public void write(JsonWriter out, Predicate value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.beginObject();
		if (value.getName().equals("bool")) {
			out.name(value.getName());
			PropertyValuePairAdapter pvpa = new PropertyValuePairAdapter();
			pvpa.write(out, (PropertyValuePair) value.getValue());
		} else {
			String queryValue;
			switch(value.getName()) {
			case "<":
				queryValue = "<";
				queryValue = queryValue + getQueryValue(value.getValue().getValue());
				break;
			case "<=":
				queryValue = "<=";
				queryValue = queryValue + getQueryValue(value.getValue().getValue());
				break;
			case ">":
				queryValue = ">";
				queryValue = queryValue + getQueryValue(value.getValue().getValue());
				break;
			case ">=":
				queryValue = ">=";
				queryValue = queryValue + getQueryValue(value.getValue().getValue());
				break;
			default:
				queryValue = (String) value.getValue().getValue();
				break;
			}
			switch(value.getName()) {
			case "=":
				String matchType = getMatchType(value);
				out.name(matchType);
				out.beginObject();
				if (matchType.equals("term")) {
					out.name(value.getValue().getProperty() + ".keyword");
				} else {
					out.name(value.getValue().getProperty());
				}
				out.value(queryValue);
				out.endObject();
				break;
			default:
				out.name("query_string");
				out.beginObject();
				out.name("default_field");
				out.value(value.getValue().getProperty());
				out.name("default_operator");
				out.value("AND");
				out.name("analyze_wildcard");
				out.value("true");
				out.name("query");

				if (queryValue.contains(":") || queryValue.contains(" ") || queryValue.contains("-")) {
					queryValue = queryValue.replaceAll(":|\\s|-", " AND ");
				}
				out.value(queryValue);
				out.endObject();
				break;
			}
		}
		out.endObject();
	}

	private String getMatchType(Predicate value) {
		String returnValue = "match_phrase";
		if (exactMatchValuesList.contains(value.getValue().getProperty())) {
			returnValue = "term";
		}
		return returnValue;
	}

	private String getQueryValue(Object value) {
		String returnValue = (String) value;
		try {
			LocalDateTime ldt = LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(returnValue));
			Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());			
			returnValue = Long.toString(d.getTime());
		} catch (DateTimeParseException e) {
			// Deliberately empty
		}
		return returnValue;
	}

}
