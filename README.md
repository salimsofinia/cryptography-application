# Cryptography Application

## Overview

The Cryptography Application is a Java-based desktop utility designed to provide robust text and file encryption and decryption functionality using a suite of classical and hybrid cryptographic algorithms. Developed as part of an academic project for the ITRI615 Cryptography module, the application aims to demonstrate practical implementations of encryption concepts through an intuitive graphical user interface (GUI).

This tool is intended for educational purposes only and should not be used in production environments requiring secure communication.

---

## Features

- Support for encryption and decryption of both plain text and file-based content.
- Multiple algorithm options, including:
  - Vigenère Cipher
  - Vernam Cipher (One-Time Pad)
  - Columnar Transposition Cipher
  - Custom Secure Cipher (CSC)
  - Vernam + Transposition Cipher (Hybrid)
- Graphical user interface with clear input/output sections.
- File validation and key verification via MD5 hashing.
- Automatic handling of non-alphabetic characters and padding.
- Informative status output for user feedback and error handling.

---

## Usage Instructions
### Text Mode
1. Select an encryption algorithm from the dropdown menu
2. Enter the encryption key
3. Enter text in the input field
4. Click Encrypt Text or Decrypt Text
5. View the result in the output field

### File Mode
1. Select an algorithm from the dropdown menu
2. Browse and select an input file
3. Enter the key and specify the output path
4. Click Encrypt File or Decrypt File
5. View logs and status messages in the output panel

---

## Authors
- Salim Sofinia
  - Columnar Transposition Cipher
  - Custom Secure Cipher
  - Vernam + Transposition Cipher
- Michael Mathebula
  - Vigenère Cipher
  - Vernam Cipher
