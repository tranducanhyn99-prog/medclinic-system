package com.medclinic.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class HmacSignatureVerifier {
    private HmacSignatureVerifier() {
    }

    public static String signSha256(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể tạo chữ ký HMAC", exception);
        }
    }

    public static boolean matchesSha256(String secret, String payload, String suppliedHex) {
        if (suppliedHex == null || !suppliedHex.matches("(?i)[0-9a-f]{64}")) return false;
        byte[] expected = HexFormat.of().parseHex(signSha256(secret, payload));
        byte[] supplied = HexFormat.of().parseHex(suppliedHex.toLowerCase());
        return MessageDigest.isEqual(expected, supplied);
    }
}
