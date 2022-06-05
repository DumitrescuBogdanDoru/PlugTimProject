package com.dbd.plugtimproject.managers;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageManager {

    private Context mContext;

    public LanguageManager(Context mContext) {
        this.mContext = mContext;
    }

    public void updateResource(String code) {
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Resources resources = mContext.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public String getCode() {
        Resources resources = mContext.getResources();
        return  resources.getConfiguration().locale.getLanguage();
    }
}
