package com.dbd.plugtimproject.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageManager {

    private Context mContext;
    private SharedPreferences sharedPreferences;

    public LanguageManager(Context mContext) {
        this.mContext = mContext;
        sharedPreferences = mContext.getSharedPreferences("shpr", Context.MODE_PRIVATE);
    }

    public void updateResource(String code) {
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Resources resources = mContext.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public String getLang() {
        return sharedPreferences.getString("lang", "en");
    }
}
