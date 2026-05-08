package nlu.fit.soft.gr5.precisionMail.repository.impl;

import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.repository.EmailRepository;

import java.util.List;

public class EmailRepositoryImpl implements EmailRepository {
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
