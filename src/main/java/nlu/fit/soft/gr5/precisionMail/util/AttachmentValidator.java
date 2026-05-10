package nlu.fit.soft.gr5.precisionMail.util;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for validating email attachments.
 * Enforces business rules from FR2.3:
 * - Maximum 10 files
 * - Total size <= 25MB
 * - Dangerous file types are rejected
 */
public class AttachmentValidator {
    
    private static final int MAX_FILE_COUNT = 10;
    private static final long MAX_TOTAL_SIZE = 25 * 1024 * 1024; // 25 MB
    
    // File extensions that are considered dangerous/executable
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
        DANGEROUS_EXTENSIONS.add(".js");  // JavaScript could be dangerous
        DANGEROUS_EXTENSIONS.add(".jse");
        DANGEROUS_EXTENSIONS.add(".ws");
        DANGEROUS_EXTENSIONS.add(".wsh");
    }
    
    /**
     * Validates a single file for attachment.
     * 
     * @param file The file to validate
     * @return ValidationResult containing success status and error message if any
     */
    public static ValidationResult validateFile(File file) {
        if (file == null) {
            return new ValidationResult(false, "File không được null");
        }
        
        if (!file.exists()) {
            return new ValidationResult(false, "File không tồn tại: " + file.getName());
        }
        
        if (!file.isFile()) {
            return new ValidationResult(false, "Đó không phải là một file: " + file.getName());
        }
        
        // Check for dangerous file extensions
        String fileName = file.getName().toLowerCase();
        for (String ext : DANGEROUS_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return new ValidationResult(false, 
                    "Định dạng file không được phép vì lý do bảo mật: " + file.getName());
            }
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validates a file to be added to the current attachment list.
     * Checks against existing attachments.
     * 
     * @param fileToAdd The file to add
     * @param currentAttachments The current list of attachments
     * @return ValidationResult containing success status and error message if any
     */
    public static ValidationResult validateFileAddition(File fileToAdd, Collection<File> currentAttachments) {
        // First, validate the file itself
        ValidationResult fileValidation = validateFile(fileToAdd);
        if (!fileValidation.isValid) {
            return fileValidation;
        }
        
        // Check if file count would exceed limit
        int newCount = (currentAttachments != null ? currentAttachments.size() : 0) + 1;
        if (newCount > MAX_FILE_COUNT) {
            return new ValidationResult(false, 
                "Không thể thêm file. Tối đa " + MAX_FILE_COUNT + " file được cho phép. " +
                "Hiện tại có " + (newCount - 1) + " file.");
        }
        
        // Calculate total size
        long totalSize = (currentAttachments != null ? 
            currentAttachments.stream().mapToLong(File::length).sum() : 0) + fileToAdd.length();
        
        if (totalSize > MAX_TOTAL_SIZE) {
            return new ValidationResult(false, 
                "Tổng dung lượng vượt quá giới hạn 25MB. " +
                "Dung lượng hiện tại: " + formatSize(totalSize - fileToAdd.length()) + 
                " + File này: " + formatSize(fileToAdd.length()) + " = " + formatSize(totalSize));
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validates the entire attachment list.
     * 
     * @param attachments The list of attachments to validate
     * @return ValidationResult containing success status and error message if any
     */
    public static ValidationResult validateAttachmentList(Collection<File> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new ValidationResult(true, null);
        }
        
        // Check file count
        if (attachments.size() > MAX_FILE_COUNT) {
            return new ValidationResult(false, 
                "Số lượng file vượt quá giới hạn: " + attachments.size() + " > " + MAX_FILE_COUNT);
        }
        
        // Check total size
        long totalSize = attachments.stream().mapToLong(File::length).sum();
        if (totalSize > MAX_TOTAL_SIZE) {
            return new ValidationResult(false, 
                "Tổng dung lượng vượt quá 25MB: " + formatSize(totalSize));
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Formats bytes to human-readable size string.
     */
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
