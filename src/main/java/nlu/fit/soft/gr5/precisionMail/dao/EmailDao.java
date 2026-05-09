package nlu.fit.soft.gr5.precisionMail.dao;

import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.util.List;

public interface EmailDao {

    Email save(Email email);

    Email findById(Long id);

    List<Email> findAllEmail();

    List<String> findAllEmailAddress();
}
