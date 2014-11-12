package com.pkg.wifitower;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Utils {

    public static void savePreferences(Context context, String key, String value){
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
    }

    public static String readPreferences(Context context , String key, String defaultValue){
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    	return sp.getString(key, defaultValue);
    }
    
    public static void clearPreferences(Context context){
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = sp.edit();
		editor.clear();
		editor.commit();
    }

}