package com.xilli.stealthnet.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.xilli.stealthnet.model.Countries;

public class ActiveServer {
    public static void saveServer(Countries countries, Context context) {
        SharedPreferences sp = context.getApplicationContext()
                .getSharedPreferences("activeServer", 0);
        SharedPreferences.Editor editor;
        editor = sp.edit();
        editor.putString("countryName", countries.getCountry1());
        editor.putString("vpnUserName", countries.getOvpnUserName1());
        editor.putString("vpnPassword", countries.getOvpnUserPassword1());
        editor.putString("config", countries.getOvpn1());
        editor.putString("flagUrl", countries.getFlagUrl1());
        editor.commit();
    }

    public static Countries getSavedServer(Context context) {
        SharedPreferences sp = context.getApplicationContext()
                .getSharedPreferences("activeServer", 0);
        Countries countries = new Countries(
                sp.getString("countryName", ""),
                sp.getString("flagUrl", ""),
                sp.getString("config", ""),
                sp.getString("vpnUserName", ""),
                sp.getString("vpnPassword", "")
        );

        return countries;
    }

    public static void deleteSaveServer(String key, Context context){
        SharedPreferences sp = context.getApplicationContext()
                .getSharedPreferences("activeServer", 0);
        SharedPreferences.Editor editor;
        editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }
}
