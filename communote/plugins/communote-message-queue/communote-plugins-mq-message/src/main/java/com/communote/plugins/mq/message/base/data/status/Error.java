package com.communote.plugins.mq.message.base.data.status;

/**
 * Error class
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Error {
	private String errorField;

	private String errorMessage;

	private String causeErrorCode;

	/**
	 * @return the errorField
	 */
	public String getErrorField() {
		return errorField;
	}

	/**
	 * @param errorField
	 *            the errorField to set
	 */
	public void setErrorField(String errorField) {
		this.errorField = errorField;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the causeErrorCode
	 */
	public String getCauseErrorCode() {
		return causeErrorCode;
	}

	/**
	 * @param causeErrorCode
	 *            the causeErrorCode to set
	 */
	public void setCauseErrorCode(String causeErrorCode) {
		this.causeErrorCode = causeErrorCode;
	}

}
