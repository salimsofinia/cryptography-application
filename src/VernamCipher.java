import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.swing.JFrame;

public class VernamCipher {
    
    // Header size in bytes (16 bytes for verification hash)
    private static final int HEADER_SIZE = 16;
    
    // Encrypt a file using the Vernam cipher with a repeating key
    public static void encryptFile(String inputFilePath, String outputFilePath, String key) throws Exception {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Encryption key cannot be empty");
        }
        
        // Generate a verification hash from the key
        byte[] keyHash = generateKeyHash(key);
        
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            
            // Write the verification hash at the beginning of the output file
            fos.write(keyHash);
            
            int byteRead;
            int keyIndex = 0;
            
            // Encrypt file byte by byte using XOR with the key
            while ((byteRead = fis.read()) != -1) {
                if (keyIndex >= key.length()) {
                    keyIndex = 0;
                }
                char keyChar = key.charAt(keyIndex);
                int keyByte = keyChar % 256;
                fos.write(byteRead ^ keyByte);
                keyIndex++;
            }
        }
    }
    
    // Decrypt a file encrypted with the Vernam cipher
    public static void decryptFile(String inputFilePath, String outputFilePath, String key, JFrame parentFrame) 
            throws Exception {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Decryption key cannot be empty");
        }
        
        // Verify the key before decrypting
        try (FileInputStream verifyStream = new FileInputStream(inputFilePath)) {
            byte[] storedHash = new byte[HEADER_SIZE];
            int bytesRead = verifyStream.read(storedHash);
            
            if (bytesRead != HEADER_SIZE) {
                throw new IOException("Invalid encrypted file format: File too small or corrupted");
            }
            
            byte[] providedKeyHash = generateKeyHash(key);
            if (!Arrays.equals(storedHash, providedKeyHash)) {
                throw new SecurityException("Invalid decryption key");
            }
        }
        
        // Decrypt the file
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            
            // Skip the header
            fis.skip(HEADER_SIZE);
            
            int byteRead;
            int keyIndex = 0;
            
            while ((byteRead = fis.read()) != -1) {
                if (keyIndex >= key.length()) {
                    keyIndex = 0;
                }
                char keyChar = key.charAt(keyIndex);
                int keyByte = keyChar % 256;
                fos.write(byteRead ^ keyByte);
                keyIndex++;
            }
        }
    }
    
    // Encrypt plaintext using the Vernam cipher with Base64 encoding for readability
    public static String encryptText(String plainText, String key) throws Exception {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Encryption key cannot be empty");
        }
        
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = new byte[plainBytes.length];
        
        for (int i = 0; i < plainBytes.length; i++) {
            encryptedBytes[i] = (byte) (plainBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    // Decrypt text that was encrypted with the Vernam cipher
    public static String decryptText(String encryptedText, String key) throws Exception {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Decryption key cannot be empty");
        }
        
        // Clean the input by trimming and removing whitespace
        encryptedText = encryptedText.trim().replaceAll("\\s+", "");
        
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] decryptedBytes = new byte[encryptedBytes.length];
            
            for (int i = 0; i < encryptedBytes.length; i++) {
                decryptedBytes[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
            }
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // This exception is thrown if the input is not valid Base64
            throw new IllegalArgumentException("The input text is not properly encrypted or has been modified", e);
        }
    }
    
    // Generate a 16-byte MD5 hash from the key for file verification
    public static byte[] generateKeyHash(String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        return digest.digest(key.getBytes(StandardCharsets.UTF_8));
    }
    
    public static void main(String[] args) {
        try {
            // === Test 1: Encrypt and Decrypt TEXT ===
            String key = "mySecretKey";
            String plainText = "Hello World!";
            System.out.println("Original Text: " + plainText);

            String encryptedText = encryptText(plainText, key);
            System.out.println("Encrypted Text (Base64): " + encryptedText);

            String decryptedText = decryptText(encryptedText, key);
            System.out.println("Decrypted Text: " + decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
