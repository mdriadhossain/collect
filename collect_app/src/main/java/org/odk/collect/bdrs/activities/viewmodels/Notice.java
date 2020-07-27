package org.odk.collect.bdrs.activities.viewmodels;

public class Notice {

    private String id, FullName, Notification, Status, DataEntryDate, NotificationReadTime;

    public Notice(String id, String FullName, String Notification, String Status, String DataEntryDate, String NotificationReadTime) {

        this.id = id;
        this.FullName = FullName;
        this.Notification = Notification;
        this.Status = Status;
        this.DataEntryDate = DataEntryDate;
        this.NotificationReadTime = NotificationReadTime;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return FullName;
    }

    public String getNotification() {
        return Notification;
    }

    public String getStatus() {
        return Status;
    }

    public String getDataEntryDate() {
        return DataEntryDate;
    }

    public String getNotificationReadTime() {
        return NotificationReadTime;
    }
}
