public class VigenereTextCipher {
      // Clean the key: keep only letters and convert to uppercase
      private static String sanitizeKey(String key) {
        StringBuilder cleanKey = new StringBuilder();
        for (char c : key.toCharArray()) {
            if (Character.isLetter(c)) {
                cleanKey.append(Character.toUpperCase(c));
            } else if (Character.isDigit(c)) {
                // Map digits to letters (0->A, 1->B, etc.)
                cleanKey.append((char) ('A' + (c - '0')));
            }
        }

        if (cleanKey.length() == 0) {
            cleanKey.append("KEY");
        }

        return cleanKey.toString();
    }

    public static String encryptText(String plainText, String key) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        String cleanKey = sanitizeKey(key);
        StringBuilder encrypted = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < plainText.length(); i++) {
            char ch = plainText.charAt(i);

            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int plainValue = ch - base;
                int keyValue = cleanKey.charAt(keyIndex % cleanKey.length()) - 'A';

                int encryptedValue = (plainValue + keyValue) % 26;
                char encryptedChar = (char)(encryptedValue + base);

                encrypted.append(encryptedChar);
                keyIndex++;
            } else {
                encrypted.append(ch);
            }
        }

        return encrypted.toString();
    }

    public static String decryptText(String encryptedText, String key) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        String cleanKey = sanitizeKey(key);
        StringBuilder decrypted = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < encryptedText.length(); i++) {
            char ch = encryptedText.charAt(i);

            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int encryptedValue = ch - base;
                int keyValue = cleanKey.charAt(keyIndex % cleanKey.length()) - 'A';

                int decryptedValue = (encryptedValue - keyValue + 26) % 26;
                char decryptedChar = (char)(decryptedValue + base);

                decrypted.append(decryptedChar);
                keyIndex++;
            } else {
                decrypted.append(ch);
            }
        }

        return decrypted.toString();
    }
}

