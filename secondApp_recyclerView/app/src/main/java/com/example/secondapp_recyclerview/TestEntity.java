package com.example.secondapp_recyclerview;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class TestEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "firstName")
    public String firstname;
    @ColumnInfo(name = "email")
    public String email;
    @ColumnInfo(name = "age")
    public String age;
    @ColumnInfo(name = "username")
    public String username;
    @ColumnInfo(name = "password")
    public String password;

    public TestEntity(int id, String name, String firstname, String email, String age, String username, String password) {
        this.id = id;
        this.name = name;
        this.firstname = firstname;
        this.email = email;
        this.age = age;
        this.username = username;
        this.password = password;
    }

    @Ignore
    public TestEntity(String name, String firstname) {
        this.name = name;
        this.firstname = firstname;
    }

    @Ignore
    public TestEntity(String email, String age, String username, String password){
        this.email = email;
        this.age = age;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}