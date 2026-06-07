package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.util.CryptoUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kiểm thử mã hóa App Password cho UC-01.
 *
 * <p>Commit UC-01 #4 - Anh Han: đảm bảo ciphertext khác plaintext, có thể
 * decrypt về password gốc và dùng IV ngẫu nhiên cho mỗi lần mã hóa.</p>
 *
 * @author Anh Han
 */
class Uc01CryptoUtilTest {

    @Test
    void encryptProducesCiphertextThatCanBeDecryptedToOriginalPassword() {
        String appPassword = "abcd efgh ijkl mnop";

        String encrypted = CryptoUtil.encrypt(appPassword);
        String decrypted = CryptoUtil.decrypt(encrypted);

        assertAll(
                () -> assertNotNull(encrypted),
                () -> assertNotEquals(appPassword, encrypted),
                () -> assertTrue(encrypted.startsWith("v2:")),
                () -> assertEquals(appPassword, decrypted)
        );
    }

    @Test
    void encryptUsesRandomIvForSamePassword() {
        String appPassword = "same-app-password";

        String firstEncrypted = CryptoUtil.encrypt(appPassword);
        String secondEncrypted = CryptoUtil.encrypt(appPassword);

        assertAll(
                () -> assertNotEquals(firstEncrypted, secondEncrypted),
                () -> assertEquals(appPassword, CryptoUtil.decrypt(firstEncrypted)),
                () -> assertEquals(appPassword, CryptoUtil.decrypt(secondEncrypted))
        );
    }
}
