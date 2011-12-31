package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

	public String VERSION = "2.0";
	public String BUILD_DATE = "2011-12-30";

	public int PROTCOL_VERSION = 6;

	public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
