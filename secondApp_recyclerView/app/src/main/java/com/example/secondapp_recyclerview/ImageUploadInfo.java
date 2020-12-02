package com.example.secondapp_recyclerview;

import android.media.Image;

public class ImageUploadInfo {
    String image_name, path;

    public ImageUploadInfo(){
    }

    public ImageUploadInfo(String image_name, String path) {
        if(image_name.trim().equals(""))
            this.image_name = "No name";

        this.image_name = image_name;
        this.path = path;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
