package nlu.fit.soft.gr5.precisionMail.service;

import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.io.IOException;
import java.util.List;

public interface HistoryService {
    List<Email> latest() throws IOException;
}
