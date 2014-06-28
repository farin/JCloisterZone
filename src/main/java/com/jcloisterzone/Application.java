package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String DEV_VERSION = "dev-snapshot";

    public String VERSION = "2.7";
    public String BUILD_DATE = "2014-06-28";

    public int PROTCOL_VERSION = 18; //18 = 2.7

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
