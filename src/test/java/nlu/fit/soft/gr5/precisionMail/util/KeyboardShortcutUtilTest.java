package nlu.fit.soft.gr5.precisionMail.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KeyboardShortcutUtilTest {

    @Test
    @DisplayName("Test Ctrl + Enter - Đúng tổ hợp")
    void testIsCtrlEnter_Valid() {
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, true, false, false);
        assertTrue(KeyboardShortcutUtil.isCtrlEnter(event));
    }

    @Test
    @DisplayName("Test Ctrl + Shift + C - Đúng tổ hợp")
    void testIsCtrlShiftC_Valid() {
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.C, true, true, false, false);
        assertTrue(KeyboardShortcutUtil.isCtrlShiftC(event));
    }

    @Test
    @DisplayName("Test Escape - Đúng phím")
    void testIsEscape_Valid() {
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);
        assertTrue(KeyboardShortcutUtil.isEscape(event));
    }
}