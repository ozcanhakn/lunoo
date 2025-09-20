package com.lumoo.Model;

public class Comment {
    String commentId;
    String userId;
    String username;
    String comment;
    String date;
    String url;
    public Comment() {}

    public Comment(String commentId, String userId, String username, String comment, String date, String url) {
        this.commentId = commentId;
        this.userId = userId;
        this.username = username;
        this.comment = comment;
        this.date = date;
        this.url = url;
    }

    // Getters and Setters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}