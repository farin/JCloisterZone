package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    //use just for dev builds
    public String DEV_VERSION = "dev-snapshot";

    public String VERSION = "4.0.2";
    public String BUILD_DATE = "2017-12-10";

    public String PROTCOL_VERSION = "4.0.0";


    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
