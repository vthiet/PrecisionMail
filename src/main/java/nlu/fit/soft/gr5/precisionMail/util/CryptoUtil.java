package nlu.fit.soft.gr5.precisionMail.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utility mã hóa và giải mã App Password cho UC-01.
 *
 * <p>Commit UC-01 #4/#16 - Anh Han: App Password không lưu plaintext trong DB;
 * dữ liệu mới dùng AES/GCM và dữ liệu cũ vẫn được hỗ trợ qua legacy decrypt.</p>
 */
public class CryptoUtil {
    private static final String SECRET_KEY_PROPERTY = "security.aes.key";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String LEGACY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String CIPHER_PREFIX = "v2:";
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Mã hóa App Password trước khi lưu cấu hình tài khoản.
     *
     * @param plainText App Password dạng rõ
     * @return ciphertext có prefix phiên bản, hoặc null nếu đầu vào null
     */
    public static String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildSecretKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return CIPHER_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot encrypt application secret.", ex);
        }
    }

    /**
     * Giải mã App Password đã lưu.
     *
     * @param cipherText ciphertext đã lưu trong DB
     * @return App Password dạng rõ, hoặc null nếu đầu vào null
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }

        try {
            if (!cipherText.startsWith(CIPHER_PREFIX)) {
                return decryptLegacy(cipherText);
            }
            byte[] payload = Base64.getDecoder().decode(cipherText.substring(CIPHER_PREFIX.length()));
            if (payload.length <= GCM_IV_BYTES) {
                throw new IllegalStateException("Invalid encrypted payload.");
            }
            byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_BYTES);
            byte[] encrypted = Arrays.copyOfRange(payload, GCM_IV_BYTES, payload.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildSecretKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot decrypt application secret.", ex);
        }
    }

    private static String decryptLegacy(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(LEGACY_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildLegacySecretKey());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot decrypt legacy application secret.", ex);
        }
    }

    private static SecretKeySpec buildSecretKey() {
        return buildSecretKey(machineBoundMaterial());
    }

    private static SecretKeySpec buildLegacySecretKey() {
        return buildSecretKey("");
    }

    private static SecretKeySpec buildSecretKey(String extraMaterial) {
        String configuredKey = AppLoaderUtil.getProperty(SECRET_KEY_PROPERTY);
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException("Missing property: " + SECRET_KEY_PROPERTY);
        }

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashedKey = sha.digest((configuredKey + "|" + extraMaterial).getBytes(StandardCharsets.UTF_8));
            byte[] aesKey = Arrays.copyOf(hashedKey, 32);
            return new SecretKeySpec(aesKey, "AES");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }

    private static String machineBoundMaterial() {
        return String.join("|",
                System.getProperty("user.name", ""),
                System.getProperty("user.home", ""),
                System.getProperty("os.name", ""),
                System.getProperty("os.arch", ""),
                System.getProperty("os.version", "")
        );
    }
}
