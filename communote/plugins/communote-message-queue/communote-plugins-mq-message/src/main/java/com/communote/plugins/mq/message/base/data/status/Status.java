package com.communote.plugins.mq.message.base.data.status;


/**
 * Status class
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Status {

	private Error[] errors;

	private String message;

	private String statusCode;

	/**
	 * @return the errors
	 */
	public Error[] getErrors() {
		return errors;
	}

	/**
	 * @param errors
	 *            the errors to set
	 */
	public void setErrors(Error[] errors) {
		this.errors = errors;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
     * Set an optional message describing the status
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            the statusCode to set
	 */
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

}
