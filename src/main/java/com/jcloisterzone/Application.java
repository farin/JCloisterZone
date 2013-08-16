package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String VERSION = "dev";
    public String BUILD_DATE = "YYYY-MM-DD";

    public int PROTCOL_VERSION = 13;

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
