package com.communote.plugins.mq.message.core.data.topic;

/**
 * Topic access rights
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class TopicRights {

	private boolean allCanRead;

	private boolean allCanWrite;

	/**
	 * @return the allCanRead
	 */
	public boolean isAllCanRead() {
		return allCanRead;
	}

	/**
	 * @param allCanRead
	 *            the allCanRead to set
	 */
	public void setAllCanRead(boolean allCanRead) {
		this.allCanRead = allCanRead;
	}

	/**
	 * @return the allCanWrite
	 */
	public boolean isAllCanWrite() {
		return allCanWrite;
	}

	/**
	 * @param allCanWrite
	 *            the allCanWrite to set
	 */
	public void setAllCanWrite(boolean allCanWrite) {
		this.allCanWrite = allCanWrite;
	}

}
