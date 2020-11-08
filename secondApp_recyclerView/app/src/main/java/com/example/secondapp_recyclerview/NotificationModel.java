package com.example.secondapp_recyclerview;

public class NotificationModel {
    private String name;
    private String hour;


    public NotificationModel(String name, String hour) {
        this.name = name;
        this.hour = hour;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }
}
