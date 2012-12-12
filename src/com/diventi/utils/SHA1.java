package com.diventi.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {

  public static String sha1(String data) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      return data;
    }
    
    digest.update(data.getBytes());
    
    byte[] hash = digest.digest();
    String encodedHash = encodeHex(hash);
    
    return encodedHash;
  }

  public static String sha1(byte[] data) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      return "x-men";
    }
    
    digest.update(data);
    
    byte[] hash = digest.digest();
    String encodedHash = encodeHex(hash);
    
    return encodedHash;
  }

  
  private static String encodeHex(byte[] bytes) {
    StringBuffer hex = new StringBuffer(bytes.length * 2);

    for (int i = 0; i < bytes.length; i++) {
        if (((int) bytes[i] & 0xff) < 0x10) {
            hex.append("0");
        }
        hex.append(Integer.toString((int) bytes[i] & 0xff, 16));
    }

    return hex.toString();
}
}
