package nlu.fit.soft.gr5.precisionMail.service;

import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface HistoryService {
    List<Email> latest() throws IOException;

    List<Email> search(HistorySearchCriteria criteria, int pageIndex, int pageSize) throws IOException;

    int count(HistorySearchCriteria criteria) throws IOException;

    Optional<Email> detail(Long id) throws IOException;

    String sanitizeHtml(String rawHtml);

    void exportCsv(List<Email> emails, Path targetFile) throws IOException;
}
