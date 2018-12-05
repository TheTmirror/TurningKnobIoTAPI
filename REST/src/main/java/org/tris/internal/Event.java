package org.tris.internal;

import java.util.Map;

public class Event {

	private String topic;
	
	private Map<String, String> values;
	
	public Event() {
		this(null, null);
	}
	
	public Event(String topic, Map<String, String> values) {
		this.topic = topic;
		this.values = values;
	}
	
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public Map<String, String> getValues() {
		return values;
	}
	public void setValues(Map<String, String> values) {
		this.values = values;
	}
	
}