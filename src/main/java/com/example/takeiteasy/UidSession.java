package com.example.takeiteasy;

import android.content.Context;
import android.content.SharedPreferences;

public class UidSession
{
    private static final String SESSION_NAME = "data";

    public static boolean putUserID(Context context, int i)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SESSION_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", i);
        return editor.commit();
    }

    public static boolean putIsAdmin(Context context, boolean b)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SESSION_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_admin", b);
        return editor.commit();
    }

    public static int readUserID(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SESSION_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id",-1);
    }

    public static boolean readIsAdmin(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SESSION_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_admin",false);
    }

    public static void clear(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SESSION_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
