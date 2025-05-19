import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

public class CustomSecureCipher {

    // === Encrypt Text ===
    public static String encryptText(String plainText, String key) throws IOException {
        byte[] data = plainText.getBytes("UTF-8");
        byte[] transformed = transformBytes(data, getKeyShift(key));
        byte[] shuffled = gridShuffle(transformed);
        return Base64.getEncoder().encodeToString(shuffled);
    }

    // === Decrypt Text ===
    public static String decryptText(String encryptedBase64, String key) throws IOException {
        byte[] shuffled = Base64.getDecoder().decode(encryptedBase64);
        byte[] unshuffled = gridUnshuffle(shuffled);
        byte[] original = reverseTransformBytes(unshuffled, getKeyShift(key));
        return new String(original, "UTF-8");
    }

    // === Encrypt File ===
    public static void encryptFile(String inputPath, String outputPath, String key) throws IOException {
        byte[] data = Files.readAllBytes(new File(inputPath).toPath());
        byte[] transformed = transformBytes(data, getKeyShift(key));
        byte[] shuffled = gridShuffle(transformed);
        Files.write(new File(outputPath).toPath(), shuffled);
    }

    // === Decrypt File ===
    public static void decryptFile(String encryptedPath, String outputPath, String key) throws IOException {
        byte[] shuffled = Files.readAllBytes(new File(encryptedPath).toPath());
        byte[] unshuffled = gridUnshuffle(shuffled);
        byte[] original = reverseTransformBytes(unshuffled, getKeyShift(key));
        Files.write(new File(outputPath).toPath(), original);
    }

    // === Byte Transformation (Shift +7 and XOR with 0x5A, then reverse) ===
    private static byte[] transformBytes(byte[] input, int keyShift) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) ((input[i] + keyShift) ^ 0x5A);
        }
        return reverseArray(output);
    }

    private static byte[] reverseTransformBytes(byte[] input, int keyShift) {
        byte[] reversed = reverseArray(input);
        byte[] output = new byte[reversed.length];
        for (int i = 0; i < reversed.length; i++) {
            output[i] = (byte) (((reversed[i] ^ 0x5A) - keyShift));
        }
        return output;
    }

    private static byte[] reverseArray(byte[] array) {
        byte[] reversed = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            reversed[i] = array[array.length - 1 - i];
        }
        return reversed;
    }

    // === Grid Shuffle (Diagonal Bottom-Left to Top-Right) ===
    private static byte[] gridShuffle(byte[] input) throws IOException {
        int columns = 12;
        int rows = (int) Math.ceil(input.length / (double) columns);
        byte[] padded = new byte[rows * columns];
        System.arraycopy(input, 0, padded, 0, input.length);
        for (int i = input.length; i < padded.length; i++) {
            padded[i] = 'X';
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Write original length first
        out.write((input.length >> 24) & 0xFF);
        out.write((input.length >> 16) & 0xFF);
        out.write((input.length >> 8) & 0xFF);
        out.write((input.length) & 0xFF);

        for (int sum = 0; sum <= rows + columns - 2; sum++) {
            int startRow = Math.min(rows - 1, sum);
            int startCol = sum - startRow;
            while (startRow >= 0 && startCol < columns) {
                out.write(padded[startRow * columns + startCol]);
                startRow--;
                startCol++;
            }
        }
        return out.toByteArray();
    }

    // === Grid Unshuffle ===
    private static byte[] gridUnshuffle(byte[] input) throws IOException {
        int originalLength = ((input[0] & 0xFF) << 24) |
                ((input[1] & 0xFF) << 16) |
                ((input[2] & 0xFF) << 8) |
                (input[3] & 0xFF);

        byte[] shuffled = new byte[input.length - 4];
        System.arraycopy(input, 4, shuffled, 0, shuffled.length);

        int columns = 12;
        int rows = (int) Math.ceil(originalLength / (double) columns);
        byte[] flat = new byte[rows * columns];

        int index = 0;
        for (int sum = 0; sum <= rows + columns - 2; sum++) {
            int startRow = Math.min(rows - 1, sum);
            int startCol = sum - startRow;
            while (startRow >= 0 && startCol < columns) {
                if (index < shuffled.length) {
                    flat[startRow * columns + startCol] = shuffled[index++];
                }
                startRow--;
                startCol++;
            }
        }

        byte[] result = new byte[originalLength];
        System.arraycopy(flat, 0, result, 0, originalLength);
        return result;
    }
    
    private static int getKeyShift(String key) {
        if (key != null && key.length() >= 7) {
            return (int) key.charAt(6);  // 7th character (index 6)
        }
        return 7; // default shift
    }

    // === Main Method ===
    public static void main(String[] args) throws IOException {
        String plainText = "Confidential documents contain sensitive information that must be securely handled to prevent unauthorized access.";
        String theKey = "Secret7Key";
        System.out.println("Original Text:\n" + plainText);
        String encryptedText = encryptText(plainText, theKey);
        System.out.println("\nEncrypted Text (Base64):\n" + encryptedText);
        String decryptedText = decryptText(encryptedText, theKey);
        System.out.println("\nDecrypted Text:\n" + decryptedText);

        // File encryption
        String inputFilePath = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\Academic Writing.pptx";
        String encryptedFilePath = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\testfile_encrypted";
        String decryptedFilePath = "C:\\Users\\S_CSIS-PostGrad\\Desktop\\testfile_decrypted.pptx";

        encryptFile(inputFilePath, encryptedFilePath, theKey);
        System.out.println("\nFile encryption done.");

        decryptFile(encryptedFilePath, decryptedFilePath, theKey);
        System.out.println("File decryption done.");
    }
}