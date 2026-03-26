package com.peerlink.security;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

public class CryptoUtils {

    private static final String CURVE_NAME = "secp256r1";
    private static final String AES_ALGO = "AES";
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits

    public static KeyPair generateECKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec(CURVE_NAME));
        return keyPairGenerator.generateKeyPair();
    }

    public static SecretKey deriveSharedKey(PrivateKey myPrivate, PublicKey peerPublic) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(myPrivate);
        keyAgreement.doPhase(peerPublic, true);
        byte[] sharedSecret = keyAgreement.generateSecret();
        
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha256.digest(sharedSecret);
        
        return new SecretKeySpec(Arrays.copyOf(keyBytes, 32), AES_ALGO);
    }

    public static byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return encryptWithIV(data, key, iv);
    }

    public static byte[] encryptWithIV(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        
        byte[] ciphertextAndTag = cipher.doFinal(data);
        byte[] result = new byte[12 + ciphertextAndTag.length];
        
        System.arraycopy(iv, 0, result, 0, 12);
        System.arraycopy(ciphertextAndTag, 0, result, 12, ciphertextAndTag.length);
        
        return result;
    }

    public static byte[] decrypt(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[12];
        System.arraycopy(data, 0, iv, 0, 12);
        
        byte[] ciphertextAndTag = new byte[data.length - 12];
        System.arraycopy(data, 12, ciphertextAndTag, 0, ciphertextAndTag.length);
        
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        
        return cipher.doFinal(ciphertextAndTag);
    }

    public static void incrementIV(byte[] iv) {
        for (int i = iv.length - 1; i >= 0; i--) {
            iv[i]++;
            if (iv[i] != 0) {
                break;
            }
        }
    }
}
