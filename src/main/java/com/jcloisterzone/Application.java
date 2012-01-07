package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

	public String VERSION = "DEV";
	public String BUILD_DATE = "2012-00-00";

	public int PROTCOL_VERSION = 7;

	public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
