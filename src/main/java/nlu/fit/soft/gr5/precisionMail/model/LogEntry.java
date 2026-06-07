package nlu.fit.soft.gr5.precisionMail.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record LogEntry(
        String timestamp,
        String level,
        String thread,
        String source,
        String message,
        String rawLine
) {
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^\\[(?<timestamp>[^]]+)] \\[(?<level>[^]]+)] \\[(?<thread>[^]]+)] \\[(?<source>[^]]+)] - (?<message>.*)$",
            Pattern.DOTALL
    );

    public static LogEntry parse(String line) {
        // BF-6.1.4: parse one complete log record, including grouped multiline stacktrace content.
        String safeLine = line == null ? "" : line;
        Matcher matcher = LOG_PATTERN.matcher(safeLine);
        if (!matcher.matches()) {
            return new LogEntry("", "", "", "", safeLine, safeLine);
        }
        return new LogEntry(
                matcher.group("timestamp"),
                matcher.group("level").trim(),
                matcher.group("thread"),
                matcher.group("source"),
                matcher.group("message"),
                safeLine
        );
    }

    public String detailText() {
        // BF-6.1.10-11 / EF-6.1.10-E1: show a grouped stacktrace or an explicit no-stacktrace state.
        if ("ERROR".equalsIgnoreCase(level) && rawLine.lines().count() > 1) {
            return rawLine;
        }
        if ("ERROR".equalsIgnoreCase(level)) {
            return rawLine + System.lineSeparator() + System.lineSeparator()
                    + "Không có Stacktrace chi tiết cho dòng log này.";
        }
        return rawLine.isBlank() ? "Không có Stacktrace chi tiết cho dòng log này." : rawLine;
    }
}
