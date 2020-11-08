package com.example.secondapp_recyclerview;

public class StorageHelper {
    private static StorageHelper single_instance=null;
    public String email, age, username, password;
    public int id;

    private StorageHelper(){

    }

    public static StorageHelper getInstance(){
        if(single_instance==null)
            single_instance = new StorageHelper();
        return single_instance;
    }

    public void setId(int id){
        this.id=id;
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
    public StorageHelper getValues(){
        return single_instance;
    }
}
