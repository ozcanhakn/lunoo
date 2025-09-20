package com.lumoo.util;

import android.content.Context;
import android.util.Log;
import android.util.Patterns;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Güvenlik işlemleri için SecurityUtils sınıfı
 * Input validation, encryption ve güvenlik kontrolleri
 */
public class SecurityUtils {
    
    private static final String TAG = "SecurityUtils";
    
    // Input validation patterns
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");
    
    // XSS ve SQL injection koruması
    private static final Pattern XSS_PATTERN = Pattern.compile(".*<script.*>.*</script>.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(".*(union|select|insert|delete|update|drop|create|alter).*", Pattern.CASE_INSENSITIVE);
    
    // Maksimum input uzunlukları
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_USERNAME_LENGTH = 30;

    /**
     * Email formatı kontrolü
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Telefon numarası formatı kontrolü
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Kullanıcı adı formatı kontrolü
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        return trimmed.length() >= 3 && 
               trimmed.length() <= MAX_USERNAME_LENGTH && 
               USERNAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Güçlü şifre kontrolü
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * XSS saldırı kontrolü
     */
    public static boolean containsXSS(String input) {
        if (input == null) return false;
        
        return XSS_PATTERN.matcher(input).matches();
    }

    /**
     * SQL injection kontrolü
     */
    public static boolean containsSQLInjection(String input) {
        if (input == null) return false;
        
        return SQL_INJECTION_PATTERN.matcher(input).matches();
    }

    /**
     * Güvenli string temizleme - XSS ve SQL injection koruması
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        
        // XSS ve SQL injection kontrolü
        if (containsXSS(input) || containsSQLInjection(input)) {
            Log.w(TAG, "Potentially malicious input detected: " + input);
            return "";
        }
        
        // HTML tag'lerini temizle
        String cleaned = input.replaceAll("<[^>]*>", "");
        
        // Özel karakterleri escape et
        cleaned = cleaned.replace("'", "''")
                        .replace("\"", "\\\"")
                        .replace("\\", "\\\\");
        
        return cleaned.trim();
    }

    /**
     * İsim formatı kontrolü
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = name.trim();
        return trimmed.length() >= 2 && 
               trimmed.length() <= MAX_NAME_LENGTH &&
               !containsXSS(trimmed) &&
               !containsSQLInjection(trimmed);
    }

    /**
     * Açıklama formatı kontrolü
     */
    public static boolean isValidDescription(String description) {
        if (description == null) return true; // Açıklama opsiyonel
        
        String trimmed = description.trim();
        return trimmed.length() <= MAX_DESCRIPTION_LENGTH &&
               !containsXSS(trimmed) &&
               !containsSQLInjection(trimmed);
    }

    /**
     * Mesaj formatı kontrolü
     */
    public static boolean isValidMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = message.trim();
        return trimmed.length() <= MAX_MESSAGE_LENGTH &&
               !containsXSS(trimmed) &&
               !containsSQLInjection(trimmed);
    }

    /**
     * Yaş kontrolü
     */
    public static boolean isValidAge(int age) {
        return age >= 18 && age <= 100;
    }

    /**
     * Cinsiyet kontrolü
     */
    public static boolean isValidGender(String gender) {
        if (gender == null) return false;
        
        String trimmed = gender.trim().toLowerCase();
        return trimmed.equals("erkek") || 
               trimmed.equals("kadın") || 
               trimmed.equals("male") || 
               trimmed.equals("female");
    }

    /**
     * MD5 hash oluşturma (şifre hash'leme için)
     */
    public static String generateMD5Hash(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "MD5 algorithm not found", e);
            return "";
        }
    }

    /**
     * SHA-256 hash oluşturma (daha güvenli hash'leme için)
     */
    public static String generateSHA256Hash(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not found", e);
            return "";
        }
    }

    /**
     * Firebase Auth token güvenlik kontrolü
     */
    public static boolean isValidFirebaseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Firebase token format kontrolü (basit)
        return token.length() > 100 && 
               token.contains(".") && 
               !containsXSS(token) &&
               !containsSQLInjection(token);
    }

    /**
     * Supabase URL güvenlik kontrolü
     */
    public static boolean isValidSupabaseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = url.trim();
        return trimmed.startsWith("https://") && 
               trimmed.contains("supabase") &&
               !containsXSS(trimmed) &&
               !containsSQLInjection(trimmed);
    }

    /**
     * Kullanıcı girişi güvenlik kontrolü
     */
    public static boolean validateUserInput(String email, String password, String username) {
        return isValidEmail(email) && 
               isValidPassword(password) && 
               isValidUsername(username);
    }

    /**
     * Profil güncelleme güvenlik kontrolü
     */
    public static boolean validateProfileUpdate(String name, String description, int age, String gender) {
        return isValidName(name) && 
               isValidDescription(description) && 
               isValidAge(age) && 
               isValidGender(gender);
    }

    /**
     * Mesaj gönderme güvenlik kontrolü
     */
    public static boolean validateMessage(String message) {
        return isValidMessage(message);
    }

    /**
     * Güvenlik log'u
     */
    public static void logSecurityEvent(String event, String details) {
        Log.w(TAG, "Security Event: " + event + " - Details: " + details);
    }
}
