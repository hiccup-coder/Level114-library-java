/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public final class HashUtils {
    private HashUtils() {
    }

    public static String sha256Hex(Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            return HashUtils.sha256Hex(data);
        } catch (Exception e) {
            return null;
        }
    }

    public static String sha256Hex(String data) {
        if (data == null) {
            return null;
        }
        return HashUtils.sha256Hex(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            return HashUtils.toHex(digest);
        } catch (Exception e) {
            return null;
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0xF];
        }
        return new String(hexChars);
    }
}

