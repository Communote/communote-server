package com.communote.plugins.mq.provider.activemq.monitor.data;

/**
 * Broker queue class, to be used during the monitoring
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class BrokerQueue {

	private String name;

	private long pendingMessagesCount;

	private long dispatchedMessagesCount;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the pendingMessagesCount
	 */
	public long getPendingMessagesCount() {
		return pendingMessagesCount;
	}

	/**
	 * @param pendingMessagesCount
	 *            the pendingMessagesCount to set
	 */
	public void setPendingMessagesCount(long pendingMessagesCount) {
		this.pendingMessagesCount = pendingMessagesCount;
	}

	/**
	 * @return the dispatchedMessagesCount
	 */
	public long getDispatchedMessagesCount() {
		return dispatchedMessagesCount;
	}

	/**
	 * @param dispatchedMessagesCount
	 *            the dispatchedMessagesCount to set
	 */
	public void setDispatchedMessagesCount(long dispatchedMessagesCount) {
		this.dispatchedMessagesCount = dispatchedMessagesCount;
	}

}
