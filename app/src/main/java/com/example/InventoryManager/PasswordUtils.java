package com.example.InventoryManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {

    /**
     * Generate a random salt
     */
    public static String generateSalt() {

        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        return bytesToHex(saltBytes);
    }

    /**
     * Hash password with salt
     */
    public static String hashPassword(String password, String salt) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            String saltedPassword = password + salt;

            byte[] hashBytes = md.digest(saltedPassword.getBytes());

            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Generate full stored password (salt:hash)
     */
    public static String generateSecurePassword(String password) {

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        return salt + ":" + hash;
    }

    /**
     * Verify password against stored value
     */
    public static boolean verifyPassword(String inputPassword, String storedPassword) {

        String[] parts = storedPassword.split(":");

        if (parts.length != 2) return false;

        String salt = parts[0];
        String storedHash = parts[1];

        String inputHash = hashPassword(inputPassword, salt);

        return inputHash.equals(storedHash);
    }

    /**
     * Helper: convert bytes to hex
     */
    private static String bytesToHex(byte[] bytes) {

        StringBuilder hex = new StringBuilder();

        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }

        return hex.toString();
    }
}