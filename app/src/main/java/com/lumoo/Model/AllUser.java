package com.lumoo.Model;

public class AllUser {
    String kullanıcıAdı, profileImage, soyad, userId, doğumTarihi,online,gender;


    public AllUser(){}

    public AllUser(String kullanıcıAdı, String profileImage, String soyad,
                   String userId, String doğumTarihi,String online, String gender) {
        this.kullanıcıAdı = kullanıcıAdı;
        this.profileImage = profileImage;
        this.soyad = soyad;
        this.userId = userId;
        this.doğumTarihi = doğumTarihi;
        this.online = online;
        this.gender = gender;
    }

    public String getKullanıcıAdı() {
        return kullanıcıAdı;
    }

    public void setKullanıcıAdı(String kullanıcıAdı) {
        this.kullanıcıAdı = kullanıcıAdı;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getSoyad() {
        return soyad;
    }

    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDoğumTarihi() {
        return doğumTarihi;
    }

    public void setDoğumTarihi(String doğumTarihi) {
        this.doğumTarihi = doğumTarihi;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
