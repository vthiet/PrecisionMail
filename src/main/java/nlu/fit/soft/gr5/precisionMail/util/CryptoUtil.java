package nlu.fit.soft.gr5.precisionMail.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class CryptoUtil {
    private static final String SECRET_KEY_PROPERTY = "security.aes.key";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildSecretKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot encrypt application secret.", ex);
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildSecretKey());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot decrypt application secret.", ex);
        }
    }

    private static SecretKeySpec buildSecretKey() {
        String configuredKey = AppLoaderUtil.getProperty(SECRET_KEY_PROPERTY);
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException("Missing property: " + SECRET_KEY_PROPERTY);
        }

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashedKey = sha.digest(configuredKey.getBytes(StandardCharsets.UTF_8));
            byte[] aesKey = Arrays.copyOf(hashedKey, 16);
            return new SecretKeySpec(aesKey, "AES");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
