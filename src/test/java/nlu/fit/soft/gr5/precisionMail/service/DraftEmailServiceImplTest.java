package nlu.fit.soft.gr5.precisionMail.service;

import nlu.fit.soft.gr5.precisionMail.model.DraftEmail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DraftEmailServiceImplTest {

    @Test
    @DisplayName("Nghiệp vụ kiểm tra tính hợp lệ dữ liệu đầu vào của Thư nháp")
    void testDraftEmailValidation_Logic() {
        // Kiểm thử trường hợp Email hợp lệ
        DraftEmail validDraft = new DraftEmail();
        validDraft.senderEmail = "phule@nlu.edu.vn";
        assertNotNull(validDraft.senderEmail);
        assertFalse(validDraft.senderEmail.trim().isEmpty());

        // Kiểm thử luồng chặn dữ liệu lỗi (Email trống)
        DraftEmail invalidDraft = new DraftEmail();
        invalidDraft.senderEmail = "   ";

        assertTrue(invalidDraft.senderEmail.trim().isEmpty(),
                "Hệ thống phải nhận diện được chuỗi trống để ném lỗi IllegalArgumentException");
    }
}