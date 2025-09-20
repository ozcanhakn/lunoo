package com.lumoo.Model;

import java.util.HashMap;
import java.util.Map;

public class Post {
    String date, description, image, uid, username, userPostKey,url;
    Map<String, Boolean> likes; // Beğenen kullanıcıların UID'leri
    Map<String, Comment> comments; // Yorumlar
    long likeCount;
    long commentCount;
    long timestamp; // Sıralama için




    // Ve bu field'ı da sınıfın başına ekleyin:

    public Post() {
        likes = new HashMap<>();
        comments = new HashMap<>();
        likeCount = 0;
        commentCount = 0;
    }

    public Post(String date, String description, String image, String uid, String username,String url) {
        this.date = date;
        this.description = description;
        this.image = image;
        this.uid = uid;
        this.username = username;
        this.likes = new HashMap<>();
        this.comments = new HashMap<>();
        this.likeCount = 0;
        this.commentCount = 0;
        this.url = url;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public void setComments(Map<String, Comment> comments) {
        this.comments = comments;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    // Beğeni durumunu kontrol etme
    public boolean isLikedByUser(String userId) {
        return likes != null && likes.containsKey(userId) && Boolean.TRUE.equals(likes.get(userId));
    }

    public String getUserPostKey() {
        return userPostKey;
    }

    public void setUserPostKey(String userPostKey) {
        this.userPostKey = userPostKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    // Firebase'den veri gelirken null kontrolü
    public void initializeEmptyFields() {
        if (likes == null) {
            likes = new HashMap<>();
        }
        if (comments == null) {
            comments = new HashMap<>();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}