package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String VERSION = "2.6";
    public String BUILD_DATE = "2014-01-08";

    public int PROTCOL_VERSION = 17; //2.6

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
