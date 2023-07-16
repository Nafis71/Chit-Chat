package com.chitchat.encryption;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    Cipher cipher, decipher;
    SecretKeySpec secretKeySpec;
    byte[] secretKey;
    public static final String ALGORITHM = "AES";

    public byte[] getSecretKey() {
        return secretKey;
    }

    public AES() {
    }
    public void init(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
        generator.init(keySize); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();
        secretKey = secKey.getEncoded();
    }
    public String encryption(String message, byte[] secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance(ALGORITHM);
        byte[] stringByte = message.getBytes();
        byte[] encryptedByte; //The number of consecutive (non-overlapping) bytes in a byte string.
        try{
            secretKeySpec = new SecretKeySpec(secretKey,ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte); //DoFinal() Finishes a multiple-part encryption or decryption operation, depending on how this cipher was initialized. DoFinal(Byte[]) Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation
        }catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw  new RuntimeException(e);
        }
        return new String(encryptedByte, StandardCharsets.ISO_8859_1);
    }
    public String decryption(String message,String secretKey, byte[] privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        decipher = Cipher.getInstance(ALGORITHM);
        byte[] encryptedByte = message.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString = null;
        byte[] decryption;
        try {
            RSA rsa = new RSA();
            byte[] decryptionKey = rsa.decrypt(Base64.getDecoder().decode(secretKey),privateKey);
            secretKeySpec = new SecretKeySpec(decryptionKey,ALGORITHM);
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption = decipher.doFinal(encryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException |
                 NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
        }
        return decryptedString;

    }
    public String decryptionSender(String message,byte[] secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        decipher = Cipher.getInstance(ALGORITHM);
        byte[] encryptedByte = message.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString;
        byte[] decryption;
        try {
            secretKeySpec = new SecretKeySpec(secretKey,ALGORITHM);
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption = decipher.doFinal(encryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return decryptedString;

    }
    public String encryptionRSA(String message, byte[] secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance(ALGORITHM);
        byte[] stringByte = Base64.getDecoder().decode(message);
        byte[] encryptedByte; //The number of consecutive (non-overlapping) bytes in a byte string.
        try{
            secretKeySpec = new SecretKeySpec(secretKey,ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte); //DoFinal() Finishes a multiple-part encryption or decryption operation, depending on how this cipher was initialized. DoFinal(Byte[]) Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation
        }catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw  new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(encryptedByte);
    }
    public String decryptionRSA(String message,byte[] secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        decipher = Cipher.getInstance(ALGORITHM);
        byte[] encryptedByte = Base64.getDecoder().decode(message);
        byte[] decryption;
        try {
            secretKeySpec = new SecretKeySpec(secretKey,ALGORITHM);
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption = decipher.doFinal(encryptedByte);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(decryption);

    }
}
