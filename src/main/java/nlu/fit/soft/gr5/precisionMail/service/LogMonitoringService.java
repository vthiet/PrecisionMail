package nlu.fit.soft.gr5.precisionMail.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface LogMonitoringService {
    List<String> readRecentLines(int maxLines) throws IOException;

    Path activeLogFile();
}
