import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.swing.JFrame;

public class VernamAndTranspositionCipher {
    private final String vernamKey;
    private final TranspositionCipher transpositionCipher;

    public VernamAndTranspositionCipher(String vernamKey, String transpositionKey) {
        this.vernamKey = vernamKey;
        this.transpositionCipher = new TranspositionCipher(transpositionKey);
    }

    // === TEXT ENCRYPTION ===
    public String encryptText(String plainText) throws Exception {
        String vernamEncrypted = VernamCipher.encryptText(plainText, vernamKey);
        return transpositionCipher.encryptText(vernamEncrypted);
    }

    // === TEXT DECRYPTION ===
    public String decryptText(String encryptedText) throws Exception {
        String transpositionDecrypted = transpositionCipher.decryptText(encryptedText);
        return VernamCipher.decryptText(transpositionDecrypted, vernamKey);
    }

    // === FILE ENCRYPTION ===
    public void encryptFile(String inputFilePath, String outputFilePath) throws Exception {
        String tempPath = "temp_transposition_encrypted.dat";

        // Step 1: Transposition encryption (raw file)
        transpositionCipher.encryptFile(inputFilePath, tempPath);

        // Step 2: Vernam encryption (adds header)
        VernamCipher.encryptFile(tempPath, outputFilePath, vernamKey);

        // Cleanup
        new File(tempPath).delete();
    }

    // === FILE DECRYPTION ===
    public void decryptFile(String inputFilePath, String outputFilePath) throws Exception {
        String tempPath = "temp_vernam_decrypted.dat";

        // Step 1: Vernam decryption (removes header)
        VernamCipher.decryptFile(inputFilePath, tempPath, vernamKey, null);

        // Step 2: Transposition decryption
        transpositionCipher.decryptFile(tempPath, outputFilePath);

        // Cleanup
        new File(tempPath).delete();
    }

    // === MAIN METHOD FOR TESTING ===
    public static void main(String[] args) throws Exception {
        String vernamKey = "mySecretKey";
        String transpositionKey = "CopyrightPerkBundles";

        VernamAndTranspositionCipher combo = new VernamAndTranspositionCipher(vernamKey, transpositionKey);

        // ==== TEXT TEST ====
        String plainText = "Confidential documents contain sensitive information.";
        System.out.println("Original Text:\n" + plainText);

        String encryptedText = combo.encryptText(plainText);
        System.out.println("\nEncrypted Text:\n" + encryptedText);

        String decryptedText = combo.decryptText(encryptedText);
        System.out.println("\nDecrypted Text:\n" + decryptedText);

        // ==== FILE TEST ====
        String inputFile = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\Academic Writing.pptx";
        String encryptedFile = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\testfile_encrypted";
        String decryptedFile = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\testfile_decrypted.pptx";

        combo.encryptFile(inputFile, encryptedFile);
        System.out.println("\nFile encryption done.");

        combo.decryptFile(encryptedFile, decryptedFile);
        System.out.println("File decryption done.");
    }
}

class VernamCipher {
    
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

class TranspositionCipher {
    private int[] keyOne;
    private int[] keyTwo;
    private int[] keyThree;

    public TranspositionCipher(String key) {
        if (key.length() != 20) {
            throw new IllegalArgumentException("ERROR!\nInvalid key: Key length must be exactly 20 characters.");
        }
        setKeyOne(key.substring(0, 9).toLowerCase());
        setKeyTwo(key.substring(9, 13).toLowerCase());
        setKeyThree(key.substring(13, 20).toLowerCase());
    }

    public void setKeyOne(String keyOne) { this.keyOne = getKeyOrder(keyOne); }
    public void setKeyTwo(String keyTwo) { this.keyTwo = getKeyOrder(keyTwo); }
    public void setKeyThree(String keyThree) { this.keyThree = getKeyOrder(keyThree); }

    public int[] getKeyOne() { return keyOne; }
    public int[] getKeyTwo() { return keyTwo; }
    public int[] getKeyThree() { return keyThree; }

    private static int[] getKeyOrder(String key) {
        Character[] characters = new Character[key.length()];
        for (int i = 0; i < key.length(); i++) characters[i] = key.charAt(i);

        Character[] sortedCharacters = characters.clone();
        Arrays.sort(sortedCharacters);

        Map<Character, Queue<Integer>> positionMap = new HashMap<>();
        for (int i = 0; i < sortedCharacters.length; i++) {
            positionMap.putIfAbsent(sortedCharacters[i], new LinkedList<>());
            positionMap.get(sortedCharacters[i]).add(i);
        }

        int[] order = new int[key.length()];
        for (int i = 0; i < characters.length; i++) {
            order[i] = positionMap.get(characters[i]).poll();
        }
        return order;
    }

    // -------------------- TEXT ENCRYPTION/DECRYPTION --------------------

    public static class EncryptedText {
        public final String text;
        public final int paddingCount;

        public EncryptedText(String text, int paddingCount) {
            this.text = text;
            this.paddingCount = paddingCount;
        }
    }

    public String encryptText(String plainText) {
        EncryptedText e1 = encrypt(plainText.getBytes(), keyOne);
        EncryptedText e2 = encrypt(e1.text.getBytes(), keyTwo);
        EncryptedText e3 = encrypt(e2.text.getBytes(), keyThree);

        return e1.paddingCount + "," + e2.paddingCount + "," + e3.paddingCount + "," + e3.text;
    }

    public String decryptText(String cipherPackage) {
        try {
            String[] parts = cipherPackage.split(",", 4);
            int padding1 = Integer.parseInt(parts[0]);
            int padding2 = Integer.parseInt(parts[1]);
            int padding3 = Integer.parseInt(parts[2]);
            String cipherText = parts[3];

            String d1 = decrypt(cipherText, keyThree, padding3);
            String d2 = decrypt(d1, keyTwo, padding2);
            String d3 = decrypt(d2, keyOne, padding1);
            return d3;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt text: " + e.getMessage());
        }
    }

    // -------------------- FILE ENCRYPTION/DECRYPTION --------------------

    public void encryptFile(String inputFilePath, String outputFilePath) {
        try {
            byte[] data = readFile(inputFilePath);

            EncryptedBytes e1 = encryptBytes(data, keyOne);
            EncryptedBytes e2 = encryptBytes(e1.data, keyTwo);
            EncryptedBytes e3 = encryptBytes(e2.data, keyThree);

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFilePath));
            dos.writeInt(e1.paddingCount);
            dos.writeInt(e2.paddingCount);
            dos.writeInt(e3.paddingCount);
            dos.write(e3.data);
            dos.close();

            System.out.println("File encrypted successfully.");
        } catch (IOException e) {
            System.out.println("File encryption failed: " + e.getMessage());
        }
    }

    public void decryptFile(String inputFilePath, String outputFilePath) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(inputFilePath));
            int padding1 = dis.readInt();
            int padding2 = dis.readInt();
            int padding3 = dis.readInt();
            byte[] encryptedData = dis.readAllBytes();
            dis.close();

            byte[] d1 = decryptBytes(encryptedData, keyThree, padding3);
            byte[] d2 = decryptBytes(d1, keyTwo, padding2);
            byte[] d3 = decryptBytes(d2, keyOne, padding1);

            writeFile(outputFilePath, d3);
            System.out.println("File decrypted successfully.");
        } catch (IOException e) {
            System.out.println("File decryption failed: " + e.getMessage());
        }
    }

    // -------------------- INTERNAL HELPERS --------------------

    private static class EncryptedBytes {
        public final byte[] data;
        public final int paddingCount;

        public EncryptedBytes(byte[] data, int paddingCount) {
            this.data = data;
            this.paddingCount = paddingCount;
        }
    }

    private static EncryptedText encrypt(byte[] bytes, int[] key) {
        int cols = key.length;
        int rows = (int) Math.ceil((double) bytes.length / cols);

        byte[][] grid = new byte[rows][cols];
        int idx = 0, padding = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (idx < bytes.length) grid[r][c] = bytes[idx++];
                else { grid[r][c] = 'X'; padding++; }
            }
        }

        StringBuilder encrypted = new StringBuilder();
        Integer[] order = new Integer[cols];
        for (int i = 0; i < cols; i++) order[i] = i;
        Arrays.sort(order, Comparator.comparingInt(i -> key[i]));

        for (int c : order) for (int r = 0; r < rows; r++) encrypted.append((char) grid[r][c]);

        return new EncryptedText(encrypted.toString(), padding);
    }

    private static String decrypt(String cipherText, int[] key, int padding) {
        int cols = key.length;
        int rows = (int) Math.ceil((double) cipherText.length() / cols);

        byte[][] grid = new byte[rows][cols];
        int idx = 0;
        Integer[] order = new Integer[cols];
        for (int i = 0; i < cols; i++) order[i] = i;
        Arrays.sort(order, Comparator.comparingInt(i -> key[i]));

        for (int c : order) for (int r = 0; r < rows; r++) grid[r][c] = (byte) cipherText.charAt(idx++);

        StringBuilder decrypted = new StringBuilder();
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) decrypted.append((char) grid[r][c]);

        if (padding > 0) decrypted.setLength(decrypted.length() - padding);
        return decrypted.toString();
    }

    private static EncryptedBytes encryptBytes(byte[] bytes, int[] key) {
        int cols = key.length;
        int rows = (int) Math.ceil((double) bytes.length / cols);

        byte[][] grid = new byte[rows][cols];
        int idx = 0, padding = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (idx < bytes.length) grid[r][c] = bytes[idx++];
                else { grid[r][c] = 'X'; padding++; }
            }
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Integer[] order = new Integer[cols];
        for (int i = 0; i < cols; i++) order[i] = i;
        Arrays.sort(order, Comparator.comparingInt(i -> key[i]));

        for (int c : order) for (int r = 0; r < rows; r++) output.write(grid[r][c]);

        return new EncryptedBytes(output.toByteArray(), padding);
    }

    private static byte[] decryptBytes(byte[] cipherBytes, int[] key, int padding) {
        int cols = key.length;
        int rows = (int) Math.ceil((double) cipherBytes.length / cols);

        byte[][] grid = new byte[rows][cols];
        int idx = 0;
        Integer[] order = new Integer[cols];
        for (int i = 0; i < cols; i++) order[i] = i;
        Arrays.sort(order, Comparator.comparingInt(i -> key[i]));

        for (int c : order) for (int r = 0; r < rows; r++) grid[r][c] = cipherBytes[idx++];

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) output.write(grid[r][c]);

        byte[] result = output.toByteArray();
        if (padding > 0) return Arrays.copyOf(result, result.length - padding);
        else return result;
    }

    private static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] bytesArray = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray);
        fis.close();
        return bytesArray;
    }

    private static void writeFile(String filePath, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(data);
        fos.close();
    }

    public static void main(String[] args) {
        String key = "CopyrightPerkBundles";
        TranspositionCipher cipher = new TranspositionCipher(key);

        // ---------------- TEXT TEST ----------------
        String plainText = "Confidential documents contain sensitive information that must be securely handled to prevent unauthorized access.";
        System.out.println("Original Text:\n" + plainText);

        String encryptedText = cipher.encryptText(plainText);
        System.out.println("\nEncrypted Text:\n" + encryptedText);

        String decryptedText = cipher.decryptText(encryptedText);
        System.out.println("\nDecrypted Text:\n" + decryptedText);

        // ---------------- FILE TEST ----------------
        String inputFilePath = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\Academic Writing.pptx";
        String encryptedFilePath = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\testfile_encrypted";
        String decryptedFilePath = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\testfile_decrypted.pptx";

        cipher.encryptFile(inputFilePath, encryptedFilePath);
        cipher.decryptFile(encryptedFilePath, decryptedFilePath);

        System.out.println("\n--- Testing complete ---");
    }
}
