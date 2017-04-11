package com.incresol.screenoutgps.modal;

public class Notifications {

    int id;
    String notification;
    String dateRead;
    String sentondate;
    boolean isRead;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getDateRead() {
        return dateRead;
    }

    public void setDateRead(String dateRead) {
        this.dateRead = dateRead;
    }

    public String getSentondate() {
        return sentondate;
    }

    public void setSentondate(String sentondate) {
        this.sentondate = sentondate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }


}
