package com.pfizer.equip.searchservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.elasticsearch.dto.Predicate;

/**
 * Base search request class.
 * 
 * @author HeinemanWP
 *
 */
public class BaseSearchRequest extends BaseSearchCondition {
	private List<BaseSearchCondition> conditions = new ArrayList<>();
	private String operator;

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
	
	/**
	 * Predicate for finding matching conditions.
	 * 
	 * @param condition
	 * @param property
	 * @param value
	 * @return true if matching condition instances found, otherwise false.
	 */
	public boolean hasCondition(String condition, String property, String value) {
		return !findCondition(condition, property, value).isEmpty();
	}

	/**
	 * Predicate for finding matching conditions.
	 * 
	 * @param condition
	 * @param property
	 * @return true if matching condition instances found, otherwise false.
	 */
	public boolean hasCondition(String condition, String property) {
		return !findCondition(condition, property).isEmpty();
	}

	/**
	 * Find conditions in this request that matches the parameters.
	 * 
	 * @param condition
	 * @param property
	 * @param value
	 * @return the matching condition instances if found, otherwise an empty list.
	 */
	public List<BaseSearchCondition> findCondition(String condition, String property, String value) {
		List<BaseSearchCondition> returnValue = new ArrayList<>();
		for (BaseSearchCondition c : conditions) {
			if ((c instanceof BaseSearchRequest) && (!((BaseSearchRequest) c).conditions.isEmpty())) {
				List<BaseSearchCondition> matches = ((BaseSearchRequest) c).findCondition(condition, property, value);
				if (!matches.isEmpty()) {
					returnValue.addAll(matches);
				}
			} else if ((c.getCondition() != null) && c.getCondition().equalsIgnoreCase(condition) &&
						(c.getProperty() != null) && c.getProperty().equalsIgnoreCase(property) &&
						(c.getValue() != null) && c.getValue().equalsIgnoreCase(value)) {
				returnValue.add(c);
			}
		}
		return returnValue;
	}

	/**
	 * Find conditions in this request that matches the parameters.
	 * 
	 * @param condition
	 * @param property
	 * @return the matching condition instances if found, otherwise an empty list.
	 */
	public List<BaseSearchCondition> findCondition(String condition, String property) {
		List<BaseSearchCondition> returnValue = new ArrayList<>();
		for (BaseSearchCondition c : conditions) {
			if ((c instanceof BaseSearchRequest) && (!((BaseSearchRequest) c).conditions.isEmpty())) {
				List<BaseSearchCondition> matches = ((BaseSearchRequest) c).findCondition(condition, property);
				if (!matches.isEmpty()) {
					returnValue.addAll(matches);
				}
			} else if ((c.getCondition() != null) && c.getCondition().equalsIgnoreCase(condition) &&
					(c.getProperty() != null) && c.getProperty().equalsIgnoreCase(property)) {
				returnValue.add(c);
			}
		}
		return returnValue;
	}

	/**
	 * Replaces the conditions matching the parameters with a new Condition.
	 * 
	 * @param condition
	 * @param property
	 * @param value
	 * @param newCondition
	 */
	public void replaceCondition(String condition, String property, BaseSearchCondition newCondition) {
		List<BaseSearchCondition> newConditions = new ArrayList<>();
		if (hasCondition(condition, property)) {
			for (BaseSearchCondition c : conditions) {
				if ((c instanceof BaseSearchRequest) && (!((BaseSearchRequest) c).conditions.isEmpty())) {
					((BaseSearchRequest) c).replaceCondition(condition, property, newCondition);
					newConditions.add(c);
				} else if ((c.getCondition() != null) && c.getCondition().equalsIgnoreCase(condition) &&
						(c.getProperty() != null) && c.getProperty().equalsIgnoreCase(property)) {
					newConditions.add(newCondition);
				} else {
					newConditions.add(c);
				}
			}
		}
		conditions = newConditions;
	}

	public boolean replaceCondition(BaseSearchCondition matchingCondition, BaseSearchCondition newCondition) {
		boolean returnValue = false;
		for (int i = 0, n = conditions.size(); i < n; i++) {
			if (matchingCondition == conditions.get(i)) {
				conditions.set(i, newCondition);
				returnValue = true;
				break;
			}
			if ((conditions.get(i) instanceof BaseSearchRequest) && (!((BaseSearchRequest) conditions.get(i)).conditions.isEmpty())) {
				returnValue = ((BaseSearchRequest) conditions.get(i)).replaceCondition(matchingCondition, newCondition);
				if (returnValue) {
					break;
				}
			}			
		}
		return returnValue;
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
		sb.append(this.getClass().getName());
		sb.append("(");
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
