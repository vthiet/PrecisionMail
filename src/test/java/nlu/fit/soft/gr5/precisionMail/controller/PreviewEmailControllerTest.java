package nlu.fit.soft.gr5.precisionMail.controller;

import nlu.fit.soft.gr5.precisionMail.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PreviewEmailControllerTest {

    @Test
    @DisplayName("Kiểm thử cấu trúc dữ liệu Email trước khi nạp vào màn hình Preview")
    void testEmailDataStructure_BeforePreview() {
        // Khởi tạo đối tượng Email khớp chuẩn 100% các tham số Constructor của nhóm bạn:
        // 1. from, 2. toLst, 3. ccLst, 4. bccLst, 5. subject, 6. content, 7. attachments, 8. sentAt
        Email mockEmail = new Email(
                "phule@nlu.edu.vn",                           // from
                Collections.singleton("receiver@nlu.edu.vn"), // toLst
                Collections.singleton("cc@nlu.edu.vn"),       // ccLst
                Collections.singleton("bcc@nlu.edu.vn"),      // bccLst
                "Tiêu đề thử nghiệm hệ thống",                 // subject
                "<p>Nội dung bức thư kiểm thử <strong>HTML</strong></p>", // content
                null,                                         // attachments
                LocalDateTime.now()                           // sentAt
        );

        // Khẳng định dữ liệu trích xuất ra khớp hoàn toàn với các thuộc tính thực tế trong Model của bạn
        assertNotNull(mockEmail);
        assertEquals("phule@nlu.edu.vn", mockEmail.from); // Khớp chuẩn thuộc tính .from công khai
        assertEquals("phule@nlu.edu.vn", mockEmail.getFrom()); // Khớp chuẩn hàm getter getFrom()
        assertTrue(mockEmail.toLst.contains("receiver@nlu.edu.vn"));
        assertTrue(mockEmail.cc.contains("cc@nlu.edu.vn"));
        assertTrue(mockEmail.bcc.contains("bcc@nlu.edu.vn"));
        assertEquals("Tiêu đề thử nghiệm hệ thống", mockEmail.subject);
        assertEquals("<p>Nội dung bức thư kiểm thử <strong>HTML</strong></p>", mockEmail.content);
    }
}