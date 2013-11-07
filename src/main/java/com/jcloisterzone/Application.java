package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

//    public String VERSION = "2.3";
//    public String BUILD_DATE = "YYYY-MM-DD";

    public String VERSION = "dev-snapshot";
    public String BUILD_DATE = "";

    public int PROTCOL_VERSION = 15;

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
