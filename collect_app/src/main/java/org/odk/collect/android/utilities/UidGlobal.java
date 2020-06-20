package org.odk.collect.android.utilities;

public class UidGlobal {
    String userID;
    private static final UidGlobal userIDGlobal = new UidGlobal();
    public static UidGlobal getInstance() {
        return userIDGlobal;
    }
    private UidGlobal() {
    }
    public void setData(String userID) {
        this.userID = userID;
    }
    public String getData() {
        return userID;
    }
}
