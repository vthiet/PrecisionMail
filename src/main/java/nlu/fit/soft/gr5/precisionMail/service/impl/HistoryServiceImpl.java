package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.HistoryService;
import nlu.fit.soft.gr5.precisionMail.service.HistorySearchCriteria;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HistoryServiceImpl implements HistoryService {
    private static final Pattern SCRIPT_BLOCK = Pattern.compile("(?is)<script[^>]*>.*?</script>");
    private static final Pattern EVENT_ATTRIBUTES = Pattern.compile("(?i)\\s+on[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)");
    private static final Pattern JAVASCRIPT_URL = Pattern.compile("(?i)javascript:");
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final EmailDao emailDao = new EmailDaoImpl();

    @Override
    public List<Email> latest() throws IOException {
        return search(new HistorySearchCriteria("", null, null), 0, 50);
    }

    @Override
    public List<Email> search(HistorySearchCriteria criteria, int pageIndex, int pageSize) throws IOException {
        return emailDao.findHistory(criteria, pageIndex, pageSize);
    }

    @Override
    public int count(HistorySearchCriteria criteria) throws IOException {
        return emailDao.countHistory(criteria);
    }

    @Override
    public Optional<Email> detail(Long id) throws IOException {
        return emailDao.findById(id);
    }

    @Override
    public String sanitizeHtml(String rawHtml) {
        String html = rawHtml == null ? "" : rawHtml;
        html = SCRIPT_BLOCK.matcher(html).replaceAll("");
        html = EVENT_ATTRIBUTES.matcher(html).replaceAll("");
        html = JAVASCRIPT_URL.matcher(html).replaceAll("");
        if (!html.toLowerCase().contains("<html")) {
            html = "<html><body style=\"font-family: sans-serif; white-space: pre-wrap;\">" + escapeIfPlainText(html) + "</body></html>";
        }
        return html;
    }

    @Override
    public void exportCsv(List<Email> emails, Path targetFile) throws IOException {
        String rows = emails.stream()
                .map(this::csvRow)
                .collect(Collectors.joining(System.lineSeparator()));
        String content = "ID,From,To,Cc,Bcc,Subject,Sent At,Status,Attachment Count"
                + System.lineSeparator()
                + rows;
        byte[] csv = content.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[UTF8_BOM.length + csv.length];
        System.arraycopy(UTF8_BOM, 0, output, 0, UTF8_BOM.length);
        System.arraycopy(csv, 0, output, UTF8_BOM.length, csv.length);
        Files.write(targetFile, output);
    }

    private String escapeIfPlainText(String value) {
        if (value.contains("<") && value.contains(">")) {
            return value;
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String csvRow(Email email) {
        return String.join(",",
                csv(email.id == null ? "" : email.id.toString()),
                csv(email.from),
                csv(String.join("; ", email.toLst == null ? List.of() : email.toLst)),
                csv(String.join("; ", email.cc == null ? List.of() : email.cc)),
                csv(String.join("; ", email.bcc == null ? List.of() : email.bcc)),
                csv(email.subject),
                csv(email.sentAt == null ? "" : email.sentAt.toString()),
                csv(email.status == null ? "" : email.status.name()),
                csv(String.valueOf(email.attachments == null ? 0 : email.attachments.size()))
        );
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
