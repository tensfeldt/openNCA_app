package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.elasticsearch.dto.Predicate;

/**
 * Comments search request
 * 
 * @author HeinemanWP
 *
 */
public class CommentsSearchRequest extends BaseSearchRequest {
	private List<String> sources = new ArrayList<>();
	private List<BaseSearchCondition> conditions = new ArrayList<>();
	private String operator;
	
	public CommentsSearchRequest() {}

	public List<String> getSources() {
		return sources;
	}

	public void setSources(List<String> sources) {
		this.sources = sources;
	}

	public List<BaseSearchCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<BaseSearchCondition> conditions) {
		this.conditions = conditions;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Override
	public Predicate toElasticSearch() {
		String conditional = "must";
		if (operator != null) {
			switch (operator.toLowerCase()) {
			case "and":
				break;
			case "or":
				conditional = "should";
				break;
			default:
				break;
			}
		}
		if (conditions.size() == 1) {
			return new Predicate("bool", conditional, conditions.get(0).toElasticSearch());
		} else if (conditions.size() > 1) {
			List<Predicate> predicates = new ArrayList<>();
			for (BaseSearchCondition condition : conditions) {
				predicates.add(condition.toElasticSearch());
			}
			return new Predicate("bool", conditional, predicates);
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CommentsSearchRequest(");
		sb.append("[");
		for (BaseSearchCondition condition : conditions) {
			sb.append(condition.toString());
			sb.append(",");
		}
		sb.append("]");
		sb.append("|");
		sb.append(operator);
		sb.append(")");
		return sb.toString();
	}

}
