package nlu.fit.soft.gr5.precisionMail.util;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AttachmentValidator {
    // 2.3.1 Giới hạn tối đa không vượt quá 10 file đính kèm
    private static final int MAX_FILE_COUNT = 10;

    // 2.3.1 Giới hạn tổng dung lượng tệp đính kèm không vượt quá 25MB
    private static final long MAX_TOTAL_SIZE = 25 * 1024 * 1024; // 25 MB

    // 2.3.2 Danh sách đen chứa các định dạng tệp nguy hiểm/thực thi bị cấm
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>();
    
    static {
        DANGEROUS_EXTENSIONS.add(".exe");
        DANGEROUS_EXTENSIONS.add(".bat");
        DANGEROUS_EXTENSIONS.add(".cmd");
        DANGEROUS_EXTENSIONS.add(".vbs");
        DANGEROUS_EXTENSIONS.add(".com");
        DANGEROUS_EXTENSIONS.add(".pif");
        DANGEROUS_EXTENSIONS.add(".scr");
        DANGEROUS_EXTENSIONS.add(".vbe");
        DANGEROUS_EXTENSIONS.add(".js");
        DANGEROUS_EXTENSIONS.add(".jse");
        DANGEROUS_EXTENSIONS.add(".ws");
        DANGEROUS_EXTENSIONS.add(".wsh");
    }


    // 2.1.8 Hệ thống kiểm tra tính hợp lệ của tệp dựa trên: sự tồn tại và cấu trúc tệp dữ liệu vật lý
    public static ValidationResult validateFile(File file) {
        if (file == null) {
            return new ValidationResult(false, "File không được null");
        }
        // Kiểm tra sự tồn tại của tệp
        if (!file.exists()) {
            return new ValidationResult(false, "File không tồn tại: " + file.getName());
        }
        
        if (!file.isFile()) {
            return new ValidationResult(false, "Đó không phải là một file: " + file.getName());
        }

        // 2.3.2 Cảnh báo bảo mật định dạng nằm trong danh sách nguy hiểm
        String fileName = file.getName().toLowerCase();
        for (String ext : DANGEROUS_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                // 2.3.2.1 Hệ thống chặn tệp ngay lập tức
                // 2.3.2.2 Hệ thống chuẩn bị thông điệp từ chối bảo mật
                return new ValidationResult(false,
                    "Định dạng file không được phép vì lý do bảo mật: " + file.getName());
            }
        }
        
        return new ValidationResult(true, null);
    }

    // 2.1.8 Kiểm tra tính hợp lệ khi người dùng cố gắng Duyệt và Thêm một file mới vào danh sách hiện tại
    public static ValidationResult validateFileAddition(File fileToAdd, Collection<File> currentAttachments) {
        ValidationResult fileValidation = validateFile(fileToAdd);
        if (!fileValidation.isValid) {
            return fileValidation;
        }

        // 2.3.1 VI PHẠM SỐ LƯỢNG: Kiểm tra xem số lượng file thêm mới có vượt quá 10 file hay không
        int newCount = (currentAttachments != null ? currentAttachments.size() : 0) + 1;
        if (newCount > MAX_FILE_COUNT) {
            return new ValidationResult(false, 
                "Không thể thêm file. Tối đa " + MAX_FILE_COUNT + " file được cho phép. " +
                "Hiện tại có " + (newCount - 1) + " file.");
        }

        // 2.3.1 VI PHẠM DUNG LƯỢNG: Tính toán tổng dung lượng mới sau khi cộng dồn file chuẩn bị thêm
        long totalSize = (currentAttachments != null ? currentAttachments.stream().mapToLong(File::length).sum() : 0) + fileToAdd.length();
        
        if (totalSize > MAX_TOTAL_SIZE) {
            // 2.3.1.1 & 2.3.1.2 Từ chối thêm tệp và trả về thông tin lỗi chi tiết giới hạn dung lượng 25MB
            return new ValidationResult(false, 
                "Tổng dung lượng vượt quá giới hạn 25MB. " +
                "Dung lượng hiện tại: " + formatSize(totalSize - fileToAdd.length()) + 
                " + File này: " + formatSize(fileToAdd.length()) + " = " + formatSize(totalSize));
        }
        
        return new ValidationResult(true, null);
    }

    //  2.1.8 Kiểm tra toàn diện danh sách tệp đính kèm một lần cuối trước khi thực thi gửi tin
    public static ValidationResult validateAttachmentList(Collection<File> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new ValidationResult(true, null);
        }

        // Kiểm tra số lượng tệp chót
        if (attachments.size() > MAX_FILE_COUNT) {
            return new ValidationResult(false, 
                "Số lượng file vượt quá giới hạn: " + attachments.size() + " > " + MAX_FILE_COUNT);
        }
        
        // Kiểm tra tổng dung lượng
        long totalSize = attachments.stream().mapToLong(File::length).sum();
        if (totalSize > MAX_TOTAL_SIZE) {
            return new ValidationResult(false, 
                "Tổng dung lượng vượt quá 25MB: " + formatSize(totalSize));
        }
        
        return new ValidationResult(true, null);
    }

    public static String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Result object for validation.
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }
}
