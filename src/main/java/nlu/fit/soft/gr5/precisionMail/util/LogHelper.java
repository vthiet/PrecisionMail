package nlu.fit.soft.gr5.precisionMail.util;

import nlu.fit.soft.gr5.precisionMail.model.Email;

public final class LogHelper {
    private LogHelper() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String visibleLocalPart = localPart.substring(0, Math.min(3, localPart.length()));
        return visibleLocalPart + "***" + email.substring(atIndex);
    }

    public static int recipientCount(Email email) {
        return sizeOf(email.toLst) + sizeOf(email.cc) + sizeOf(email.bcc);
    }

    public static int attachmentCount(Email email) {
        return email.attachments == null ? 0 : email.attachments.size();
    }

    private static int sizeOf(java.util.Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }
}
