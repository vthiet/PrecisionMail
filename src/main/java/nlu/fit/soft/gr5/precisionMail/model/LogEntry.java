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
            "^\\[(?<timestamp>[^]]+)] \\[(?<level>[^]]+)] \\[(?<thread>[^]]+)] \\[(?<source>[^]]+)] - (?<message>.*)$"
    );

    public static LogEntry parse(String line) {
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
}
