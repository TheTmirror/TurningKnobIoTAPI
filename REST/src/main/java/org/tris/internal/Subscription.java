package org.tris.internal;

public class Subscription {
	
	private String subscriberIdentifier;
	private String topic;
	private String callbackAddress;
	private int port;
	private long bootid;
	
	public String getSubscriberIdentifier() {
		return subscriberIdentifier;
	}
	public void setSubscriberIdentifier(String subscriberIdentifier) {
		this.subscriberIdentifier = subscriberIdentifier;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getCallbackAddress() {
		return callbackAddress;
	}
	public void setCallbackAddress(String callbackAddress) {
		this.callbackAddress = callbackAddress;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public long getBootid() {
		return bootid;
	}
	public void setBootid(long bootid) {
		this.bootid = bootid;
	}
	
}