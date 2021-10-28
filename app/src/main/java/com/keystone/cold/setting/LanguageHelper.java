/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.setting;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Locale;

public class LanguageHelper {
    public static final String ENGLISH = "en";
    public static final String SIMPLIFIED_CHINESE = "zh_rCN";
    public static final String KOREA = "ko_rKR";
    public static final String SPANISH = "es_rES";
    public static final String GERMANY = "de_rDE";
    public static final String RUSSIAN = "ru_rRU";
    public static final String JAPAN = "ja_rJP";
    public static Locale defaultLocale = Locale.ENGLISH;
    private static final String TAG = "Vault.LanguageHelper";
    private static final HashMap<String, Locale> mAllLanguages = new HashMap<String, Locale>() {{
        put(ENGLISH, Locale.ENGLISH);
        put(SIMPLIFIED_CHINESE, Locale.SIMPLIFIED_CHINESE);
        put(KOREA, Locale.KOREA);
        put(SPANISH, new Locale("ES"));
        put(GERMANY, Locale.GERMANY);
        put(RUSSIAN, new Locale("RU"));
        put(JAPAN, Locale.JAPAN);
    }};

    public static Context updateResources(Context context, String language) {
        Resources resources = context.getResources();
        Locale locale = getLocaleByLanguage(language);

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }

    public static Locale getLocaleByLanguage(String language) {
        if (mAllLanguages.containsKey(language)) {
            return mAllLanguages.get(language);
        } else {
            Locale locale = Locale.getDefault();
            for (String key : mAllLanguages.keySet()) {
                if (TextUtils.equals(mAllLanguages.get(key).getLanguage(), locale.getLanguage())) {
                    return locale;
                }
            }
        }
        return Locale.ENGLISH;
    }
}
