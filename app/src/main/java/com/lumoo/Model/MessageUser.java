package com.lumoo.Model;

public class MessageUser {
    public String username, profileImage, uid, lastMessage, lastMessageTime, frame;
    public int unreadCount = 0;
    public boolean hasFrame = false;
    public long lastMessageTimestamp;

    public MessageUser(){
    }

    public MessageUser(String username, String profileImage, String uid) {
        this.username = username;
        this.profileImage = profileImage;
        this.uid = uid;
    }

    public MessageUser(String username, String profileImage, String uid, String lastMessage, String lastMessageTime, String frame, int unreadCount, boolean hasFrame, long lastMessageTimestamp) {
        this.username = username;
        this.profileImage = profileImage;
        this.uid = uid;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.frame = frame;
        this.unreadCount = unreadCount;
        this.hasFrame = hasFrame;
        this.lastMessageTimestamp = lastMessageTimestamp;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isHasFrame() {
        return hasFrame;
    }

    public void setHasFrame(boolean hasFrame) {
        this.hasFrame = hasFrame;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}