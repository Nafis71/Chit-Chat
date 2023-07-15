package com.chitchat;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSA {
    PrivateKey privateKey;
    PublicKey publicKey;

    public RSA() {
    }
    public void init(int keySize){
        try{
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public byte[] encrypt(byte[] text, byte[] publicKeys) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance("RSA");
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeys));
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        return cipher.doFinal(text);

    }
    public byte[] decrypt(byte[] text, byte[] privateKeys) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        Cipher decipher = Cipher.getInstance("RSA");
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeys));
        decipher.init(Cipher.DECRYPT_MODE,privateKey);
        return decipher.doFinal(text);
    }

    public byte[] getPrivateKey() {
        return privateKey.getEncoded();
    }

    public byte[] getPublicKey() {
        return publicKey.getEncoded();
    }

}
