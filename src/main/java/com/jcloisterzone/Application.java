package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String VERSION = "2.1";
    public String BUILD_DATE = "2012-06-09";

    public int PROTCOL_VERSION = 11;

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
