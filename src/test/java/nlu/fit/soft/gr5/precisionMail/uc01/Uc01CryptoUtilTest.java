package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.util.CryptoUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void rejectsTamperedCiphertext() {
        String encrypted = CryptoUtil.encrypt("app-password");
        char replacement = encrypted.endsWith("A") ? 'B' : 'A';
        String tampered = encrypted.substring(0, encrypted.length() - 1) + replacement;

        assertThrows(IllegalStateException.class, () -> CryptoUtil.decrypt(tampered));
    }

    @Test
    void preservesNullValues() {
        assertAll(
                () -> assertNull(CryptoUtil.encrypt(null)),
                () -> assertNull(CryptoUtil.decrypt(null))
        );
    }
}
