package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String VERSION = "2.1-prev1";
    public String BUILD_DATE = "2012-05-21";

    public int PROTCOL_VERSION = 8;

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
