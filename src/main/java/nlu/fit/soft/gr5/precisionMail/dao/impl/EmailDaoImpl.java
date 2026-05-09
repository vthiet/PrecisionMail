package nlu.fit.soft.gr5.precisionMail.dao.impl;

import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;

import java.util.List;

public class EmailDaoImpl implements EmailDao {
    @Override
    public Email save(Email email) {
        return null;
    }

    @Override
    public Email findById(Long id) {
        return null;
    }

    @Override
    public List<Email> findAllEmail() {
        return List.of();
    }

    @Override
    public List<String> findAllEmailAddress() {
        return List.of();
    }
}
