import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

// Only keep the file encryption/decryption parts here
public class VigenereCipher {

    public static void encryptFile(String inputFilePath, String outputFilePath, String key) 
            throws IOException, NoSuchAlgorithmException {
        File inputFile = new File(inputFilePath);
        byte[] fileBytes = Files.readAllBytes(inputFile.toPath());

        byte[] keyHash = MessageDigest.getInstance("MD5").digest(key.getBytes());
        byte[] keyBytes = key.getBytes();

        try (FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {
            outputStream.write(keyHash);

            for (int i = 0; i < fileBytes.length; i++) {
                byte keyByte = keyBytes[i % keyBytes.length];
                byte encryptedByte = (byte)(fileBytes[i] ^ keyByte);
                outputStream.write(encryptedByte);
            }
        }
    }

    public static void decryptFile(String inputFilePath, String outputFilePath, String key, JFrame parentFrame) 
            throws IOException, NoSuchAlgorithmException {
        File inputFile = new File(inputFilePath);
        byte[] fileBytes = Files.readAllBytes(inputFile.toPath());

        if (fileBytes.length < 16) {
            throw new IOException("Invalid encrypted file: File is too small");
        }

        byte[] storedHash = new byte[16];
        System.arraycopy(fileBytes, 0, storedHash, 0, 16);
        byte[] providedKeyHash = MessageDigest.getInstance("MD5").digest(key.getBytes());

        if (!Arrays.equals(storedHash, providedKeyHash)) {
            throw new SecurityException("Invalid decryption key");
        }

        byte[] keyBytes = key.getBytes();

        try (FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {
            for (int i = 16; i < fileBytes.length; i++) {
                byte keyByte = keyBytes[(i-16) % keyBytes.length];
                byte decryptedByte = (byte)(fileBytes[i] ^ keyByte);
                outputStream.write(decryptedByte);
            }
        }
    }
}
