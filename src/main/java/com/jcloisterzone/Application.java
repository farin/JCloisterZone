package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String DEV_VERSION = "dev-snapshot";

//    public String VERSION = "2.7-RC1";
//    public String BUILD_DATE = "2014-06-26";

    public String VERSION = DEV_VERSION;
    public String BUILD_DATE = "";

    public String PROTCOL_VERSION = "3.0.2";

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
