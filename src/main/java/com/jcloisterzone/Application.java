package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    //use just for dev builds
    public String DEV_VERSION = "dev-snapshot";

//    public String VERSION = "X.Y.Z";
//    public String BUILD_DATE = "YYYY-MM-DD";

    public String VERSION = DEV_VERSION;
    public String BUILD_DATE = "";

    public String PROTCOL_VERSION = "4.1.0";

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
