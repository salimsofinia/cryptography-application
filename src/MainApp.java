import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import static javax.swing.JOptionPane.showMessageDialog;
import java.security.NoSuchAlgorithmException;

public class MainApp extends JFrame {
    // Text encryption components
    private JComboBox<String> textAlgorithmSelector;
    private JTextField textKeyField;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JButton encryptTextButton;
    private JButton decryptTextButton;
    
    // File encryption components
    private JComboBox<String> fileAlgorithmSelector;
    private JTextField fileKeyField;
    private JTextField inputFileField;
    private JTextField outputFileField;
    private JButton inputBrowseButton;
    private JButton outputBrowseButton;
    private JButton encryptFileButton;
    private JButton decryptFileButton;
    
    // Status components
    private JTextArea statusArea;
    
    public MainApp() {
        setTitle("File & Text Encryption Application");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add left panel (text operations) and right panel (file operations)
        mainPanel.add(createTextPanel());
        mainPanel.add(createFilePanel());
        
        // Add status panel at the bottom
        JPanel statusPanel = createStatusPanel();
        
        // Add panels to the frame
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTextPanel() {
        JPanel textPanel = new JPanel(new BorderLayout(10, 10));
        textPanel.setBorder(BorderFactory.createTitledBorder("Text Encryption/Decryption"));
        
        // Algorithm selection
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algorithmPanel.add(new JLabel("Algorithm:"));
        textAlgorithmSelector = new JComboBox<>(new String[] {
            "Vigenère Text Cipher",
            "Vernam Cipher",
            "Transposition Cipher",
            "Custom Cipher",
            "Vernam + Transposition Cipher"
        });
        algorithmPanel.add(textAlgorithmSelector);
        
        // Key field
        JPanel keyPanel = new JPanel(new BorderLayout(5, 0));
        keyPanel.add(new JLabel("Encryption Key:"), BorderLayout.WEST);
        textKeyField = new JTextField(10);
        keyPanel.add(textKeyField, BorderLayout.CENTER);
        
        // Combine algorithm and key panels
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        topPanel.add(algorithmPanel);
        topPanel.add(keyPanel);
        
        // Text areas
        JPanel textAreasPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        
        // Input text area
        JPanel inputTextPanel = new JPanel(new BorderLayout(5, 5));
        inputTextPanel.add(new JLabel("Input Text:"), BorderLayout.NORTH);
        inputTextArea = new JTextArea(4, 15);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputTextPanel.add(inputScrollPane, BorderLayout.CENTER);
        
        // Output text area
        JPanel outputTextPanel = new JPanel(new BorderLayout(5, 5));
        outputTextPanel.add(new JLabel("Output Text:"), BorderLayout.NORTH);
        outputTextArea = new JTextArea(4, 15);
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputTextPanel.add(outputScrollPane, BorderLayout.CENTER);
        
        textAreasPanel.add(inputTextPanel);
        textAreasPanel.add(outputTextPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        encryptTextButton = new JButton("Encrypt Text");
        encryptTextButton.setPreferredSize(new Dimension(120, 30));
        encryptTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                encryptText();
            }
        });
        
        decryptTextButton = new JButton("Decrypt Text");
        decryptTextButton.setPreferredSize(new Dimension(120, 30));
        decryptTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decryptText();
            }
        });
        
        buttonPanel.add(encryptTextButton);
        buttonPanel.add(decryptTextButton);
        
        // Combine all panels
        textPanel.add(topPanel, BorderLayout.NORTH);
        textPanel.add(textAreasPanel, BorderLayout.CENTER);
        textPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return textPanel;
    }
    
    private JPanel createFilePanel() {
        JPanel filePanel = new JPanel(new BorderLayout(10, 10));
        filePanel.setBorder(BorderFactory.createTitledBorder("File Encryption/Decryption"));
    
        // Top Section: Algorithm selection + Key
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 5));
    
        // Algorithm selection
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algorithmPanel.add(new JLabel("Algorithm:"));
        fileAlgorithmSelector = new JComboBox<>(new String[] {
            "Vigenère Cipher",
            "Vernam Cipher",
            "Transposition Cipher",
            "Custom Cipher",
            "Vernam + Transposition Cipher"
        });
        fileAlgorithmSelector.setPreferredSize(new Dimension(180, 25));
        algorithmPanel.add(fileAlgorithmSelector);
    
        // Key input
        JPanel keyPanel = new JPanel(new BorderLayout(5, 0));
        keyPanel.add(new JLabel("Encryption Key:"), BorderLayout.WEST);
        fileKeyField = new JTextField(10);
        keyPanel.add(fileKeyField, BorderLayout.CENTER);
    
        topPanel.add(algorithmPanel);
        topPanel.add(keyPanel);
    
        // Center Section: Input/Output file fields
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
    
        // Input File
        JPanel inputFilePanel = new JPanel(new BorderLayout(5, 5));
        inputFilePanel.add(new JLabel("Input File:"), BorderLayout.WEST);
        inputFileField = new JTextField();
        inputFileField.setEditable(false);
        inputFilePanel.add(inputFileField, BorderLayout.CENTER);
        inputBrowseButton = new JButton("Browse");
        inputBrowseButton.setPreferredSize(new Dimension(90, 10));
        inputBrowseButton.addActionListener(e -> selectInputFile());
        inputFilePanel.add(inputBrowseButton, BorderLayout.EAST);
    
        // Output File
        JPanel outputFilePanel = new JPanel(new BorderLayout(5, 5));
        outputFilePanel.add(new JLabel("Output File:"), BorderLayout.WEST);
        outputFileField = new JTextField();
        outputFilePanel.add(outputFileField, BorderLayout.CENTER);
        outputBrowseButton = new JButton("Browse");
        outputBrowseButton.setPreferredSize(new Dimension(90, 10));
        outputBrowseButton.addActionListener(e -> selectOutputFile());
        outputFilePanel.add(outputBrowseButton, BorderLayout.EAST);
    
        centerPanel.add(inputFilePanel);
        centerPanel.add(outputFilePanel);
    
        // Bottom Section: Encrypt/Decrypt buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        encryptFileButton = new JButton("Encrypt File");
        encryptFileButton.setPreferredSize(new Dimension(130, 35));
        encryptFileButton.addActionListener(e -> encryptFile());
    
        decryptFileButton = new JButton("Decrypt File");
        decryptFileButton.setPreferredSize(new Dimension(130, 35));
        decryptFileButton.addActionListener(e -> decryptFile());
    
        buttonPanel.add(encryptFileButton);
        buttonPanel.add(decryptFileButton);
    
        // Arrange all sections
        filePanel.add(topPanel, BorderLayout.NORTH);
        filePanel.add(centerPanel, BorderLayout.CENTER);
        filePanel.add(buttonPanel, BorderLayout.SOUTH);
    
        return filePanel;
    }
    
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        statusArea = new JTextArea(6, 80);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusPanel.add(statusScrollPane, BorderLayout.CENTER);
        return statusPanel;
    }
    
    private void selectInputFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            inputFileField.setText(selectedFile.getAbsolutePath());
            
            // Auto-generate output file name (same path, add .enc extension)
            String inputPath = selectedFile.getAbsolutePath();
            outputFileField.setText(inputPath + ".enc");
        }
    }
    
    
    private void selectOutputFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void encryptText() {
        try {
            String inputText = inputTextArea.getText();
            String key = textKeyField.getText();
            
            if (inputText.isEmpty()) {
                showError("Please enter text to encrypt.");
                return;
            }
            
            if (key.isEmpty()) {
                showError("Please enter an encryption key.");
                return;
            }
            
            String algorithm = (String) textAlgorithmSelector.getSelectedItem();
            String encryptedText;
            
            switch (algorithm) { // rule switch
                case "Vigenère Text Cipher" -> encryptedText = VigenereTextCipher.encryptText(inputText, key);
                case "Vernam Cipher" -> encryptedText = VernamCipher.encryptText(inputText, key);
                case "Transposition Cipher" -> encryptedText = new TranspositionCipher(key).encryptText(inputText);
                case "Custom Cipher" -> encryptedText = CustomSecureCipher.encryptText(inputText, key);
                case "Vernam + Transposition Cipher" -> encryptedText = new VernamAndTranspositionCipher(key, key).encryptText(inputText);
                default -> throw new IllegalArgumentException("Unknown algorithm selected");
            }
            
            outputTextArea.setText(encryptedText);
            statusArea.setText("Text encrypted successfully using " + algorithm + ".");
        } catch (Exception e) {
            showError("Encryption failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // In MainApp.java, find the decryptText() method and replace it with:

private void decryptText() {
    try {
        String algorithm = (String) textAlgorithmSelector.getSelectedItem();
        String key = textKeyField.getText();
        
        if (key.isEmpty()) {
            showError("Please enter a decryption key.");
            return;
        }
        
        String textToDecrypt;
        
        // Check if there's text in the output area first (likely from a previous encryption)
        if (outputTextArea.getText().trim().length() > 0) {
            textToDecrypt = outputTextArea.getText();
            // Move encrypted text to input area for clarity
            inputTextArea.setText(textToDecrypt);
        } else {
            textToDecrypt = inputTextArea.getText();
        }
        
        if (textToDecrypt.isEmpty()) {
            showError("Please enter encrypted text to decrypt.");
            return;
        }
        
        String decryptedText;
        switch (algorithm) {
            case "Vigenère Text Cipher" -> decryptedText = VigenereTextCipher.decryptText(textToDecrypt, key);
            case "Vernam Cipher" -> decryptedText = VernamCipher.decryptText(textToDecrypt, key);
            case "Transposition Cipher" -> decryptedText = new TranspositionCipher(key).decryptText(textToDecrypt);
            case "Custom Cipher" -> decryptedText = CustomSecureCipher.decryptText(textToDecrypt, key);
            case "Vernam + Transposition Cipher" -> decryptedText = new VernamAndTranspositionCipher(key, key).decryptText(textToDecrypt);
            default -> throw new IllegalArgumentException("Unknown algorithm selected");
        }

        outputTextArea.setText(decryptedText);
        statusArea.setText("Text decrypted successfully using " + algorithm + ".");
    } catch (Exception e) {
        showError("Decryption failed: " + e.getMessage());
        e.printStackTrace();
    }
}


    private void encryptFile() {
        if (!validateFileInputs()) return;
        
        try {
            String algorithm = (String) fileAlgorithmSelector.getSelectedItem();
            String inputFilePath = inputFileField.getText();
            String outputFilePath = outputFileField.getText();
            String key = fileKeyField.getText();
            
            statusArea.setText("Starting file encryption process...\n");
            
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        switch (algorithm) {
                            case "Vigenère Cipher" -> VigenereCipher.encryptFile(inputFilePath, outputFilePath, key);
                            case "Vernam Cipher" -> VernamCipher.encryptFile(inputFilePath, outputFilePath, key);
                            case "Transposition Cipher" -> new TranspositionCipher(key).encryptFile(inputFilePath, outputFilePath);
                            case "Custom Cipher" -> CustomSecureCipher.encryptFile(inputFilePath, outputFilePath, key);
                            case "Vernam + Transposition Cipher" -> new VernamAndTranspositionCipher(key, key).encryptFile(inputFilePath, outputFilePath);
                            default -> throw new IllegalArgumentException("Unknown algorithm selected");
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            statusArea.append("Encryption completed successfully!\n");
                            statusArea.append("Output saved to: " + outputFilePath);
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            statusArea.append("ERROR: " + e.getMessage() + "\n");
                            e.printStackTrace();
                        });
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            showError("Error initiating encryption: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void decryptFile() {
        if (!validateFileInputs()) return;
        
        try {
            String algorithm = (String) fileAlgorithmSelector.getSelectedItem();
            String inputFilePath = inputFileField.getText();
            String outputFilePath = outputFileField.getText();
            String key = fileKeyField.getText();
            
            statusArea.setText("Starting file decryption process...\n");
            
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        switch (algorithm) {
                            case "Vigenère Cipher" -> VigenereCipher.decryptFile(inputFilePath, outputFilePath, key, MainApp.this);
                            case "Vernam Cipher" -> VernamCipher.decryptFile(inputFilePath, outputFilePath, key, MainApp.this);
                            case "Transposition Cipher" -> new TranspositionCipher(key).decryptFile(inputFilePath, outputFilePath);
                            case "Custom Cipher" -> CustomSecureCipher.decryptFile(inputFilePath, outputFilePath, key);
                            case "Vernam + Transposition Cipher" -> new VernamAndTranspositionCipher(key, key).decryptFile(inputFilePath, outputFilePath);
                            default -> throw new IllegalArgumentException("Unknown algorithm selected");
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            statusArea.append("Decryption completed successfully!\n");
                            statusArea.append("Output saved to: " + outputFilePath);
                        });
                    } catch (SecurityException e) {
                        SwingUtilities.invokeLater(() -> {
                            statusArea.append("Decryption failed: Invalid key provided\n");
                            JOptionPane.showMessageDialog(
                                MainApp.this,
                                "Decryption failed: Invalid key provided",
                                "Decryption Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            statusArea.append("ERROR: " + e.getMessage() + "\n");
                            JOptionPane.showMessageDialog(
                                MainApp.this,
                                "Error: " + e.getMessage(),
                                "Decryption Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            showError("Error initiating decryption: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateFileInputs() {
        if (inputFileField.getText().isEmpty()) {
            showError("Please select an input file.");
            return false;
        }
        if (outputFileField.getText().isEmpty()) {
            showError("Please specify an output file.");
            return false;
        }
        if (fileKeyField.getText().isEmpty()) {
            showError("Please enter an encryption key.");
            return false;
        }
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setLocationRelativeTo(null); // Center on screen
            app.setVisible(true);
        });
    }
}