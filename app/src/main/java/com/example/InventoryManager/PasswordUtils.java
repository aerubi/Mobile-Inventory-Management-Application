package com.example.InventoryManager;

import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility class for handling password security.
 *
 * Provides:
 * - Salt generation
 * - Secure password hashing (PBKDF2)
 * - Password verification
 *
 * NOTE:
 * PBKDF2 is used instead of simple hashing (like SHA-256)
 * because it is slower and more resistant to brute-force attacks.
 */
public class PasswordUtils {

    // Number of hashing iterations (higher = more secure, but slower)
    private static final int ITERATIONS = 10000;

    // Length of generated hash
    private static final int KEY_LENGTH = 256;

    /**
     * Generates a random salt value.
     * Salt is used to prevent identical passwords from having the same hash.
     */
    public static String generateSalt() {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];

        // Fill array with random bytes
        random.nextBytes(salt);

        return bytesToHex(salt);
    }

    /**
     * Hash a password using PBKDF2 with a provided salt.
     *
     * @param password User's plain text password
     * @param salt     Random salt value
     * @return Hashed password (hex string)
     */
    public static String hashPassword(String password, String salt) {

        try {
            // Convert password into character array and combine with salt
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    hexToBytes(salt),
                    ITERATIONS,
                    KEY_LENGTH
            );

            // Use PBKDF2 algorithm with SHA-256
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            // Generate hash
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return bytesToHex(hash);

        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Generates the full stored password format.
     *
     * Format:
     * salt:hash
     *
     * This allows us to store both values together.
     */
    public static String generateSecurePassword(String password) {

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        return salt + ":" + hash;
    }

    /**
     * Verifies a user-entered password against the stored value.
     *
     * Steps:
     * 1. Split stored value into salt + hash
     * 2. Re-hash input password using same salt
     * 3. Compare hashes
     *
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String input, String stored) {

        String[] parts = stored.split(":");

        // Ensure correct format
        if (parts.length != 2) return false;

        String salt = parts[0];
        String storedHash = parts[1];

        // Hash input password using same salt
        String inputHash = hashPassword(input, salt);

        return inputHash.equals(storedHash);
    }

    /**
     * Helper method: Convert byte array to hex string.
     */
    private static String bytesToHex(byte[] bytes) {

        StringBuilder hex = new StringBuilder();

        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);

            // Ensure two-character formatting
            if (s.length() == 1) hex.append('0');

            hex.append(s);
        }

        return hex.toString();
    }

    /**
     * Helper method: Convert hex string back to byte array.
     */
    private static byte[] hexToBytes(String hex) {

        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(
                    hex.substring(2 * i, 2 * i + 2), 16
            );
        }

        return bytes;
    }
}