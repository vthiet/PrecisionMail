package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.HistoryService;

import java.io.IOException;
import java.util.List;

public class HistoryServiceImpl implements HistoryService {
    private final EmailDao emailDao = new EmailDaoImpl();

    @Override
    public List<Email> latest() throws IOException {
        return emailDao.findAll();
    }
}
