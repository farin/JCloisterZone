package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

	//use just for dev builds
    public String DEV_VERSION = "dev-snapshot";

    public String VERSION = "3.4.3";
    public String BUILD_DATE = "2017-05-08";

    public String PROTCOL_VERSION = "3.4.2";


    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
