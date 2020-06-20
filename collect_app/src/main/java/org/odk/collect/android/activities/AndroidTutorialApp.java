package org.odk.collect.android.activities;
 
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
 
public class AndroidTutorialApp extends Application {
 
	// add this variable declaration:
	public static int uid ;
	//abbb
	//public static int fid ;
	
	//public static String formname;
	public static String fullid;
	public static String fid;
	public static int i=0;
	public static ArrayList<String> arrayList = new ArrayList<String>();
	public static ArrayList<String> fullidList = new ArrayList<String>();
	public static ArrayList<String> rootList = new ArrayList<String>();
	//public static List<String> a = new ArrayList<String>();
	
	private static AndroidTutorialApp singleton;
 
	public static AndroidTutorialApp getInstance() {
		return singleton;
	}
 
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
	}
}