package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

	public String VERSION = "2.0-SNAPSHOT";
	public String BUILD_DATE = "YYYY-MM-DD";

	public int PROTCOL_VERSION = 6;

	public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
