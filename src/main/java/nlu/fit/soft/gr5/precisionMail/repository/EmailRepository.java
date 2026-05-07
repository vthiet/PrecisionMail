package nlu.fit.soft.gr5.precisionMail.repository;

import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.util.List;

public interface EmailRepository {

    public Email save(Email email);

    public Email findById(Long id);

    public List<Email> findAllEmail();
}
