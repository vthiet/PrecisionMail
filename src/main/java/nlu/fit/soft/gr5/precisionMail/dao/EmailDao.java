package nlu.fit.soft.gr5.precisionMail.dao;

import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.HistorySearchCriteria;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface EmailDao {

    Email save(Email email) throws IOException;

    List<Email> findAll() throws IOException;

    List<Email> findHistory(HistorySearchCriteria criteria, int pageIndex, int pageSize) throws IOException;

    int countHistory(HistorySearchCriteria criteria) throws IOException;

    Optional<Email> findById(Long id) throws IOException;

    List<String> findAllEmailAddress() throws IOException;
}
