package com.jcloisterzone.ui;

import java.util.Locale;
import java.util.MissingResourceException;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

public class I18nUtils {

    private static I18n i18n;
    private static Locale locale = Locale.getDefault();

    public static String _(String s, Object... args) {
        if (i18n == null) {
            try {
                i18n = I18nFactory.getI18n(Client.class, "Messages", locale);
            } catch (MissingResourceException ex) {
                i18n = I18nFactory.getI18n(Client.class, "Messages", Locale.ENGLISH, I18nFactory.FALLBACK);
            }
        }
        return i18n.tr(s, args);
    }

    public static void setLocale(Locale locale) {
        I18nUtils.locale = locale;
        i18n = null;
    }

}
