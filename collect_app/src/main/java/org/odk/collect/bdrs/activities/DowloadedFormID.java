package org.odk.collect.bdrs.activities;

import android.app.Application;

import java.util.ArrayList;

public class DowloadedFormID extends Application {

    //add this variable declaration:
    public static int frmid ;
    //public static int fid ;

    //public static String formname;
    public static String fullid;
    public static String fid;
    public static int i=0;
    public static ArrayList<String> arrayList = new ArrayList<String>();
    public static ArrayList<String> fullidList = new ArrayList<String>();
    public static ArrayList<String> rootList = new ArrayList<String>();
    //public static List<String> a = new ArrayList<String>();

    private static DowloadedFormID singleton;

    public static DowloadedFormID getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
