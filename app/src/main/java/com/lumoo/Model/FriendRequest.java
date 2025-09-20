package com.lumoo.Model;

public class FriendRequest {
    String name,surname,username,profileImage,uid;

    public FriendRequest(){}

    public FriendRequest(String name, String surname, String username, String profileImage, String uid) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.profileImage = profileImage;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
