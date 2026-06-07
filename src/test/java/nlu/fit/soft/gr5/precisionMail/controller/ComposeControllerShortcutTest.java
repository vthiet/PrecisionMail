package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nlu.fit.soft.gr5.precisionMail.util.KeyboardShortcutUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComposeControllerShortcutTest {

    @Test
    @DisplayName("Kiểm thử tổ hợp phím tắt chức năng gửi mail nhanh (Ctrl + Enter)")
    void testCtrlEnterShortcut_ShouldReturnTrue() {
        // Giả lập sự kiện nhấn phím Ctrl + Enter độc lập, không cần chạy màn hình UI thật
        KeyEvent event = new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "",
                KeyCode.ENTER,
                false, true, false, false // controlDown = true
        );

        // Gọi hàm kiểm tra tiện ích từ util của nhóm bạn
        boolean isTriggered = KeyboardShortcutUtil.isCtrlEnter(event);

        // Khẳng định phím tắt hoạt động chính xác
        assertTrue(isTriggered, "Hệ thống phải nhận diện chính xác tổ hợp phím Ctrl + Enter");
    }
}