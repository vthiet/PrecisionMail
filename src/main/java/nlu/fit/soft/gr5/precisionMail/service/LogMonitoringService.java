package nlu.fit.soft.gr5.precisionMail.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface LogMonitoringService {
    List<String> readRecentLines(int maxLines) throws IOException;

    List<String> streamAndFilterLogs(String level, String keyword, int maxLines) throws IOException;

    Path exportActiveLogs(Path destination) throws IOException;

    LogWatchRegistration watchActiveLog(Consumer<List<String>> newLinesConsumer) throws IOException;

    Path activeLogFile();

    Path activeLogDirectory();

    boolean isActiveLogOversized() throws IOException;

    interface LogWatchRegistration extends AutoCloseable {
        @Override
        void close();
    }
}
