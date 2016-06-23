package com.communote.plugins.mq.message.base.handler;

import java.util.Set;

/**
 * represents result of a message validation
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class ValidationResult {

	/**
	 * validation status
	 */
	public enum ValidationStatus {
		/**
		 * valid
		 */
		VALID,

		/**
		 * invalid
		 */
		INVALID
	}

	private ValidationStatus validationStatus;

	private Set<String> messages;

	/**
	 * @return the message
	 */
	public Set<String> getMessages() {
		return messages;
	}

	/**
	 * @param messages
	 *            the message to set
	 */
	public void setMessages(Set<String> messages) {
		this.messages = messages;
	}

	/**
	 * @return the validationStatus
	 */
	public ValidationStatus getValidationStatus() {
		return validationStatus;
	}

	/**
	 * @param validationStatus
	 *            the validationStatus to set
	 */
	public void setValidationStatus(ValidationStatus validationStatus) {
		this.validationStatus = validationStatus;
	}

}
