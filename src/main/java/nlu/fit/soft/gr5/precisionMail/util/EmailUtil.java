package nlu.fit.soft.gr5.precisionMail.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmailUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtil.class);
    // Chuỗi Regex phục vụ kiểm tra cấu trúc email hợp lệ
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_EXTRACT_PATTERN =
            Pattern.compile("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+");

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static Set<String> emailFeature(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(plainText.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // 2.1.3 & 2.1.5 Ràng buộc kích hoạt nút Gửi (Mọi email nhập vào phải đúng định dạng)
    public static boolean containsOnlyValidEmails(String plainText) {
        Set<String> emails = emailFeature(plainText);
        if (emails.isEmpty()) {
            return true;
        }

        return emails.stream().allMatch(EmailUtil::isValidEmail);
    }

    // 2.1.3 & 2.1.5 Ràng buộc kích hoạt nút Gửi (Có ít nhất một địa chỉ người nhận)
    public static boolean hasAnyValidEmail(String plainText) {
        return emailFeature(plainText).stream().anyMatch(EmailUtil::isValidEmail);
    }

    // 2.2.2 Trích xuất các địa chỉ email hợp lệ bằng bộ lọc từ nội dung tệp thô
    public static Set<String> extractEmails(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return Set.of();
        }

        LinkedHashSet<String> emails = new LinkedHashSet<>();
        Matcher matcher = EMAIL_EXTRACT_PATTERN.matcher(rawText);
        while (matcher.find()) {
            String email = matcher.group().trim();
            if (isValidEmail(email)) {
                emails.add(email);
            }
        }
        return emails; // Trả về danh sách đã lọc
    }

    public static InternetAddress[] parseAddresses(Set<String> emails) {
        return emails.stream()
                .map(email -> {
                    try {
                        InternetAddress addr = new InternetAddress(email.trim());
                        addr.validate();
                        return addr;
                    } catch (AddressException e) {
                        throw new IllegalArgumentException("Invalid email: " + email, e);
                    }
                })
                .toArray(InternetAddress[]::new);
    }

    public static Session getSession(Account account) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        LOGGER.debug("Creating SMTP session for sender={}.", LogHelper.maskEmail(account.getUsername()));

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account.getUsername(), account.getPassword());
            }
        });
    }

    // Post-condition: Đóng gói vào đối tượng Email và sẵn sàng để gửi qua SMTP
    public static void send(Account account, Email email) throws MessagingException, IOException {
        Set<String> toList = email.toLst;
        Set<String> ccList = email.cc;
        Set<String> bccList = email.bcc;

        Session session = getSession(account);
        MimeMessage message = new MimeMessage(session);

        // 2.1.4 Đóng gói danh sách người nhận chính (To)
        if (toList != null && !toList.isEmpty()) {
            message.setRecipients(Message.RecipientType.TO, parseAddresses(toList));
        }
        // 2.1.4 Đóng gói danh sách đồng gửi công khai (Cc)
        if (ccList != null && !ccList.isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, parseAddresses(ccList));
        }
        // 2.1.4 Đóng gói danh sách gửi ẩn danh (Bcc)
        if (bccList != null && !bccList.isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, parseAddresses(bccList));
        }

        // 2.1.2 Thiết lập tài khoản gửi thư (From)
        message.setFrom(new InternetAddress(account.getUsername()));

        // 2.1.6 Thiết lập tiêu đề thư (Subject)
        message.setSubject(email.subject);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart textPart = new MimeBodyPart();

        // 2.1.6 Thiết lập nội dung văn bản thư (Content)
        textPart.setText(email.content);

        multipart.addBodyPart(textPart);

        // 2.1.9 Nếu tệp đính kèm hợp lệ, tiến hành đóng gói chúng vào cấu trúc Mail để gửi đi
        if (email.attachments != null) {
            for (String attachFilePath : email.attachments) {
                File file = new File(attachFilePath);

                // 2.1.8 Kiểm tra sự tồn tại vật lý của tệp trên ổ đĩa trước khi nạp
                if (!file.exists()) {
                    LOGGER.warn("Attachment skipped because file does not exist. path={}", attachFilePath);
                    continue;
                }

                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                // Tên file tiếng Việt
                attachmentPart.setFileName(MimeUtility.encodeText(file.getName()));

                multipart.addBodyPart(attachmentPart);
            }
        }

        message.setContent(multipart);
        message.setSentDate(new Date());

        Transport.send(message);
        LOGGER.debug("SMTP transport completed successfully for sender={}.", LogHelper.maskEmail(account.getUsername()));
    }
}
