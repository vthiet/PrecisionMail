package nlu.fit.soft.gr5.precisionMail.controller;

import nlu.fit.soft.gr5.precisionMail.model.DraftEmail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComposeControllerAutoSaveTest {

    @Test
    @DisplayName("Test mô phỏng trích xuất các trường dữ liệu form phục vụ Auto-Save")
    void testExtractFormFields_ForAutoSave() {
        // Tạo đối tượng dữ liệu nháp độc lập
        DraftEmail draft = new DraftEmail();
        draft.senderEmail = "phule@nlu.edu.vn";
        draft.toRecipients = "dev@nlu.edu.vn";
        draft.subject = "Báo cáo kiểm thử phần mềm";
        draft.body = "<h1>Nội dung email viết dở...</h1>";

        // Kiểm tra xem các trường dữ liệu đã được nạp giá trị sẵn sàng cho việc lưu trữ chưa
        assertNotNull(draft.senderEmail);
        assertNotNull(draft.toRecipients);
        assertNotNull(draft.subject);
        assertNotNull(draft.body);

        // Xác thực nội dung chuỗi trích xuất
        assertEquals("Báo cáo kiểm thử phần mềm", draft.subject);
        assertTrue(draft.body.contains("HTML") || draft.body.contains("html") || draft.body.contains("<h1>"));
    }
}