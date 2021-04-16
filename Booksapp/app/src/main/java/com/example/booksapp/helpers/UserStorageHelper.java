package com.example.booksapp.helpers;

public class UserStorageHelper {
    private static UserStorageHelper single_instance;
    public String idUserFirebase, email, age, username, password;
    public int id;

    private UserStorageHelper(){
    }

    public static UserStorageHelper getInstance(){
        if(single_instance==null)
            single_instance = new UserStorageHelper();
        return single_instance;
    }

    public void setId(int id){
        this.id=id;
    }
    public void setIdUserFirebase(String idUserFirebase){
        this.idUserFirebase = idUserFirebase;
    }
    public void setEmail(String email){
        this.email=email;
    }
    public void setAge(String age){
        this.age=age;
    }
    public void setUsername(String username){
        this.username=username;
    }
    public void setPassword(String password){
        this.password=password;
    }
    public UserStorageHelper getValues(){
        return single_instance;
    }
    public String getUsername(){
        return username;
    }

    public String getIdUserFirebase() {
        return idUserFirebase;
    }

    public String getEmail() {
        return email;
    }

    public String getAge() {
        return age;
    }

    public String getPassword() {
        return password;
    }
}