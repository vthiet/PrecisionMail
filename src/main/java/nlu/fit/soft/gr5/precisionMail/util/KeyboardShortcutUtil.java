package nlu.fit.soft.gr5.precisionMail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Utility class for managing keyboard shortcuts in Compose Email UI
 *
 * Shortcuts:
 * - Ctrl + Enter: Send Email
 * - Ctrl + A: Attach File
 * - Ctrl + Shift + C: Toggle CC field
 * - Ctrl + Shift + B: Toggle BCC field
 * - Escape: Cancel Compose
 */
public class KeyboardShortcutUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyboardShortcutUtil.class);

    /**
     * Check if Ctrl+Enter is pressed
     */
    public static boolean isCtrlEnter(KeyEvent event) {
        return event.isControlDown() && event.getCode() == KeyCode.ENTER;
    }

    /**
     * Check if Ctrl+A is pressed
     */
    public static boolean isCtrlA(KeyEvent event) {
        return event.isControlDown() && event.getCode() == KeyCode.A && !event.isShiftDown();
    }

    /**
     * Check if Ctrl+Shift+C is pressed (Toggle CC)
     */
    public static boolean isCtrlShiftC(KeyEvent event) {
        return event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.C;
    }

    /**
     * Check if Ctrl+Shift+B is pressed (Toggle BCC)
     */
    public static boolean isCtrlShiftB(KeyEvent event) {
        return event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.B;
    }

    /**
     * Check if Escape is pressed (Cancel)
     */
    public static boolean isEscape(KeyEvent event) {
        return event.getCode() == KeyCode.ESCAPE;
    }

    /**
     * Check if Ctrl+S is pressed (Save Draft - reserved for future use)
     */
    public static boolean isCtrlS(KeyEvent event) {
        return event.isControlDown() && event.getCode() == KeyCode.S && !event.isShiftDown();
    }

    /**
     * Log keyboard shortcut activation
     */
    public static void logShortcutUsed(String shortcutName) {
        LOGGER.debug("Keyboard shortcut activated: {}", shortcutName);
    }
}
