package com.lumoo.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Input validation ve form kontrolü için ValidationUtils sınıfı
 * Kullanıcı girdilerini güvenli şekilde doğrular
 */
public class ValidationUtils {
    
    private static final String TAG = "ValidationUtils";

    /**
     * Boş string kontrolü
     */
    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Null veya boş kontrolü
     */
    public static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Email formatı ve güvenlik kontrolü
     */
    public static ValidationResult validateEmail(String email) {
        if (isEmpty(email)) {
            return new ValidationResult(false, "Email adresi boş olamaz");
        }
        
        if (!SecurityUtils.isValidEmail(email)) {
            return new ValidationResult(false, "Geçerli bir email adresi giriniz");
        }
        
        if (SecurityUtils.containsXSS(email)) {
            return new ValidationResult(false, "Geçersiz karakterler tespit edildi");
        }
        
        return new ValidationResult(true, "Email geçerli");
    }

    /**
     * Şifre güvenlik kontrolü
     */
    public static ValidationResult validatePassword(String password) {
        if (isEmpty(password)) {
            return new ValidationResult(false, "Şifre boş olamaz");
        }
        
        if (password.length() < 8) {
            return new ValidationResult(false, "Şifre en az 8 karakter olmalıdır");
        }
        
        if (!SecurityUtils.isValidPassword(password)) {
            return new ValidationResult(false, "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir");
        }
        
        if (SecurityUtils.containsXSS(password)) {
            return new ValidationResult(false, "Geçersiz karakterler tespit edildi");
        }
        
        return new ValidationResult(true, "Şifre geçerli");
    }

    /**
     * Kullanıcı adı kontrolü
     */
    public static ValidationResult validateUsername(String username) {
        if (isEmpty(username)) {
            return new ValidationResult(false, "Kullanıcı adı boş olamaz");
        }
        
        if (!SecurityUtils.isValidUsername(username)) {
            return new ValidationResult(false, "Kullanıcı adı 3-20 karakter arası olmalı ve sadece harf, rakam ve _ içermelidir");
        }
        
        if (SecurityUtils.containsXSS(username)) {
            return new ValidationResult(false, "Geçersiz karakterler tespit edildi");
        }
        
        return new ValidationResult(true, "Kullanıcı adı geçerli");
    }

    /**
     * İsim kontrolü
     */
    public static ValidationResult validateName(String name) {
        if (isEmpty(name)) {
            return new ValidationResult(false, "İsim boş olamaz");
        }
        
        if (!SecurityUtils.isValidName(name)) {
            return new ValidationResult(false, "İsim 2-50 karakter arası olmalıdır");
        }
        
        if (SecurityUtils.containsXSS(name)) {
            return new ValidationResult(false, "Geçersiz karakterler tespit edildi");
        }
        
        return new ValidationResult(true, "İsim geçerli");
    }

    /**
     * Yaş kontrolü
     */
    public static ValidationResult validateAge(int age) {
        if (!SecurityUtils.isValidAge(age)) {
            return new ValidationResult(false, "Yaş 18-100 arası olmalıdır");
        }
        
        return new ValidationResult(true, "Yaş geçerli");
    }

    /**
     * Cinsiyet kontrolü
     */
    public static ValidationResult validateGender(String gender) {
        if (isEmpty(gender)) {
            return new ValidationResult(false, "Cinsiyet seçilmelidir");
        }
        
        if (!SecurityUtils.isValidGender(gender)) {
            return new ValidationResult(false, "Geçerli bir cinsiyet seçiniz");
        }
        
        return new ValidationResult(true, "Cinsiyet geçerli");
    }

    /**
     * Açıklama kontrolü
     */
    public static ValidationResult validateDescription(String description) {
        if (description == null) {
            return new ValidationResult(true, "Açıklama opsiyonel");
        }
        
        if (!SecurityUtils.isValidDescription(description)) {
            return new ValidationResult(false, "Açıklama çok uzun veya geçersiz karakterler içeriyor");
        }
        
        return new ValidationResult(true, "Açıklama geçerli");
    }

    /**
     * Mesaj kontrolü
     */
    public static ValidationResult validateMessage(String message) {
        if (isEmpty(message)) {
            return new ValidationResult(false, "Mesaj boş olamaz");
        }
        
        if (!SecurityUtils.isValidMessage(message)) {
            return new ValidationResult(false, "Mesaj çok uzun veya geçersiz karakterler içeriyor");
        }
        
        return new ValidationResult(true, "Mesaj geçerli");
    }

    /**
     * Telefon numarası kontrolü
     */
    public static ValidationResult validatePhone(String phone) {
        if (isEmpty(phone)) {
            return new ValidationResult(false, "Telefon numarası boş olamaz");
        }
        
        if (!SecurityUtils.isValidPhone(phone)) {
            return new ValidationResult(false, "Geçerli bir telefon numarası giriniz");
        }
        
        return new ValidationResult(true, "Telefon numarası geçerli");
    }

    /**
     * Firebase kullanıcı kontrolü
     */
    public static boolean isUserAuthenticated() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        return user != null && !user.isAnonymous();
    }

    /**
     * Firebase kullanıcı UID kontrolü
     */
    public static ValidationResult validateUserId(String userId) {
        if (isEmpty(userId)) {
            return new ValidationResult(false, "Kullanıcı ID boş olamaz");
        }
        
        if (userId.length() < 20) {
            return new ValidationResult(false, "Geçersiz kullanıcı ID");
        }
        
        if (SecurityUtils.containsXSS(userId) || SecurityUtils.containsSQLInjection(userId)) {
            return new ValidationResult(false, "Geçersiz karakterler tespit edildi");
        }
        
        return new ValidationResult(true, "Kullanıcı ID geçerli");
    }

    /**
     * Supabase URL kontrolü
     */
    public static ValidationResult validateSupabaseUrl(String url) {
        if (isEmpty(url)) {
            return new ValidationResult(false, "URL boş olamaz");
        }
        
        if (!SecurityUtils.isValidSupabaseUrl(url)) {
            return new ValidationResult(false, "Geçerli bir Supabase URL'i giriniz");
        }
        
        return new ValidationResult(true, "URL geçerli");
    }

    /**
     * Kayıt formu kontrolü
     */
    public static ValidationResult validateRegistrationForm(String email, String password, String username, String name) {
        ValidationResult emailResult = validateEmail(email);
        if (!emailResult.isValid()) {
            return emailResult;
        }
        
        ValidationResult passwordResult = validatePassword(password);
        if (!passwordResult.isValid()) {
            return passwordResult;
        }
        
        ValidationResult usernameResult = validateUsername(username);
        if (!usernameResult.isValid()) {
            return usernameResult;
        }
        
        ValidationResult nameResult = validateName(name);
        if (!nameResult.isValid()) {
            return nameResult;
        }
        
        return new ValidationResult(true, "Tüm alanlar geçerli");
    }

    /**
     * Profil güncelleme formu kontrolü
     */
    public static ValidationResult validateProfileUpdateForm(String name, String description, int age, String gender) {
        ValidationResult nameResult = validateName(name);
        if (!nameResult.isValid()) {
            return nameResult;
        }
        
        ValidationResult descriptionResult = validateDescription(description);
        if (!descriptionResult.isValid()) {
            return descriptionResult;
        }
        
        ValidationResult ageResult = validateAge(age);
        if (!ageResult.isValid()) {
            return ageResult;
        }
        
        ValidationResult genderResult = validateGender(gender);
        if (!genderResult.isValid()) {
            return genderResult;
        }
        
        return new ValidationResult(true, "Profil güncelleme formu geçerli");
    }

    /**
     * Validation sonucu sınıfı
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
