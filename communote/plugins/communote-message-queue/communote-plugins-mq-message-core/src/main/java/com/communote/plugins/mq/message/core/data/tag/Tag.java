package com.communote.plugins.mq.message.core.data.tag;


/**
 * The Class Tag.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class Tag {

	/** The tag store id. */
	private Long tagStoreId;

	/** The tag store alias. */
	private String tagStoreAlias;

	/** The tag store tag id. */
	private String tagStoreTagId;

	/** The language code. */
	private String languageCode;

	/** The descriptions. */
	private String description;

	/** The names. */
	private String name;
	
	/**
	 * The default name
	 */
	private String defaultName;

	/** The id. */
	private Long id;

	/**
	 * Gets the tag store id.
	 * 
	 * @return the tagStoreId
	 */
	public Long getTagStoreId() {
		return tagStoreId;
	}

	/**
	 * Sets the tag store id.
	 * 
	 * @param tagStoreId
	 *            the tagStoreId to set
	 */
	public void setTagStoreId(Long tagStoreId) {
		this.tagStoreId = tagStoreId;
	}

	/**
	 * Gets the tag store alias.
	 * 
	 * @return the tagStoreAlias
	 */
	public String getTagStoreAlias() {
		return tagStoreAlias;
	}

	/**
	 * Sets the tag store alias.
	 * 
	 * @param tagStoreAlias
	 *            the tagStoreAlias to set
	 */
	public void setTagStoreAlias(String tagStoreAlias) {
		this.tagStoreAlias = tagStoreAlias;
	}

	/**
	 * Gets the tag store tag id.
	 * 
	 * @return the tagStoreTagId
	 */
	public String getTagStoreTagId() {
		return tagStoreTagId;
	}

	/**
	 * Sets the tag store tag id.
	 * 
	 * @param tagStoreTagId
	 *            the tagStoreTagId to set
	 */
	public void setTagStoreTagId(String tagStoreTagId) {
		this.tagStoreTagId = tagStoreTagId;
	}

	/**
	 * Gets the language code.
	 * 
	 * @return the languageCode
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * Sets the language code.
	 * 
	 * @param languageCode
	 *            the languageCode to set
	 */
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	/**
	 * Gets the default description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the default description.
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the default name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the default name.
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the defaultName
	 */
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * @param defaultName the defaultName to set
	 */
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}
	
	

}
