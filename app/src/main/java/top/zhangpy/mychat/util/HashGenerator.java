package top.zhangpy.mychat.util;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {

    private static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Logger.e("HashGenerator", "SHA-256 algorithm not found");
        }
    }

    public static String getPasswordHash(String password) {
        byte[] passwordBytes = password.getBytes();
        if (md == null) {
            return null;
        }
        byte[] passwordHash = md.digest(passwordBytes);
        StringBuilder passwordHashString = new StringBuilder();
        for (byte b : passwordHash) {
            passwordHashString.append(String.format("%02x", b));
        }
        return passwordHashString.toString();
    }
}
