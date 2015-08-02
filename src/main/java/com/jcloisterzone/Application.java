package com.jcloisterzone;

/**
 * Various application constants.
 * @author Roman Krejcik
 */
public interface Application {

    public String DEV_VERSION = "dev-snapshot";

    public String VERSION = "3.3.0-beta2";
    public String BUILD_DATE = "2015-08-02";

    public String PROTCOL_VERSION = "3.2.99"; //means beta

    public static final String ILLEGAL_STATE_MSG = "Method '{}' called in invalid state";
}
