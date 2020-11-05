package com.example.firstapp;

import androidx.recyclerview.widget.RecyclerView;

public class ListModel{
    private String name;
    private String firstname;
    private int age;

    public ListModel(String name, String firstname, int age) {
        this.name = name;
        this.firstname = firstname;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
