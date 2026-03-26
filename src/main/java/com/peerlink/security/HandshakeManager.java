package com.peerlink.security;

import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class HandshakeManager {

    public static SecretKey senderHandshake(DataInputStream in, DataOutputStream out) throws Exception {
        KeyPair keyPair = CryptoUtils.generateECKeyPair();
        byte[] myEncodedPublic = keyPair.getPublic().getEncoded();
        
        out.writeInt(myEncodedPublic.length);
        out.write(myEncodedPublic);
        out.flush();
        
        int peerPubLen = in.readInt();
        byte[] peerPubBytes = in.readNBytes(peerPubLen);
        
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey peerPublic = keyFactory.generatePublic(new X509EncodedKeySpec(peerPubBytes));
        
        return CryptoUtils.deriveSharedKey(keyPair.getPrivate(), peerPublic);
    }

    public static SecretKey receiverHandshake(DataInputStream in, DataOutputStream out) throws Exception {
        KeyPair keyPair = CryptoUtils.generateECKeyPair();
        
        int peerPubLen = in.readInt();
        byte[] peerPubBytes = in.readNBytes(peerPubLen);
        
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey peerPublic = keyFactory.generatePublic(new X509EncodedKeySpec(peerPubBytes));
        
        byte[] myEncodedPublic = keyPair.getPublic().getEncoded();
        out.writeInt(myEncodedPublic.length);
        out.write(myEncodedPublic);
        out.flush();
        
        return CryptoUtils.deriveSharedKey(keyPair.getPrivate(), peerPublic);
    }
}
