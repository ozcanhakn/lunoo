package com.lumoo.Model;

public class LiveStreamChat {
    public String userName, userPhoto, userMessage;


    public LiveStreamChat(){}


    public LiveStreamChat(String userName, String userPhoto, String userMessage) {
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.userMessage = userMessage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
}
