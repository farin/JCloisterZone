package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String VERSION = "2.4.1";
    public String BUILD_DATE = "2013-10-10";

    public int PROTCOL_VERSION = 15;

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
