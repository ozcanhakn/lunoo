package com.lumoo.Model;

public class Publisher {
    private String userId;
    private String userName;
    private String roomId;
    private String streamerPhoto;

    // Getters ve Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getStreamerPhoto() {
        return streamerPhoto;
    }

    public void setStreamerPhoto(String streamerPhoto) {
        this.streamerPhoto = streamerPhoto;
    }
}
