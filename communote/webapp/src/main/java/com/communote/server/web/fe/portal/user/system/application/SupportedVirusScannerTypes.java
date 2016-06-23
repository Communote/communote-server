package com.communote.server.web.fe.portal.user.system.application;

/**
 * Property constants that represents types of anti virus scanner the
 * application supports.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public enum SupportedVirusScannerTypes {
	/** Clam AntiVirus */
	NO_SCANNER("com.communote.common.virusscan.impl.NoneVirusScanner"),

	/** Clam AntiVirus */
	CLAMAV("com.communote.common.virusscan.impl.ClamAVScanner"),

	/** Command Line AntiVirus Tool */
	CMDLINE("com.communote.common.virusscan.impl.CommandlineScanner");

	/** the class name of the anti virus scanner */
	private final String scannerClassName;

	/**
	 * Constructor for this enum type.
	 * 
	 * @param scannerClassName
	 *            the class name as string
	 */
	private SupportedVirusScannerTypes(String scannerClassName) {
		this.scannerClassName = scannerClassName;
	}

	/**
	 * The class name as string assigned to the constant.
	 * 
	 * @return the scanner class name as string
	 */
	public String getScannerClassName() {
		return scannerClassName;
	}
}