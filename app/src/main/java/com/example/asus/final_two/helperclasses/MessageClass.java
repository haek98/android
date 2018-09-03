package com.example.asus.final_two.helperclasses;

public class MessageClass {
    public String name, message;
    public String imageUri;
    public String uid;

    MessageClass() {

    }

    public MessageClass(String name, String message, String imageUri, String uid) {
        this.name = name;
        this.message = message;
        this.imageUri = imageUri;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
