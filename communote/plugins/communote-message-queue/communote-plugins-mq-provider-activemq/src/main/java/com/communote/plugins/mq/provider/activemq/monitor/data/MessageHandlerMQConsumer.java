package com.communote.plugins.mq.provider.activemq.monitor.data;

/**
 * Represents MQ Consumer. To be used during monitoring
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class MessageHandlerMQConsumer implements Comparable<MessageHandlerMQConsumer> {
	
	private String selector;

	private long dispatchedMessagesCount;

	private long pendingMessagesCount;

	/**
	 * @return the selector
	 */
	public String getSelector() {
		return selector;
	}

	/**
	 * @param selector
	 *            the selector to set
	 */
	public void setSelector(String selector) {
		this.selector = selector;
	}

	/**
	 * @return the dispatchedMessagesCount
	 */
	public long getDispatchedMessagesCount() {
		return dispatchedMessagesCount;
	}

	/**
	 * @param dispatchedMessagesCount the dispatchedMessagesCount to set
	 */
	public void setDispatchedMessagesCount(long dispatchedMessagesCount) {
		this.dispatchedMessagesCount = dispatchedMessagesCount;
	}

	/**
	 * @return the pendingMessagesCount
	 */
	public long getPendingMessagesCount() {
		return pendingMessagesCount;
	}

	/**
	 * @param pendingMessagesCount the pendingMessagesCount to set
	 */
	public void setPendingMessagesCount(long pendingMessagesCount) {
		this.pendingMessagesCount = pendingMessagesCount;
	}

	@Override
	public int compareTo(MessageHandlerMQConsumer o) {
		return selector.compareTo(o.selector);
	}



}
