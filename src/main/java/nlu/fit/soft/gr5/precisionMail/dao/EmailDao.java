package nlu.fit.soft.gr5.precisionMail.dao;

import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.io.IOException;
import java.util.List;

public interface EmailDao {

    Email save(Email email) throws IOException;

    List<Email> findAll() throws IOException;

    List<String> findAllEmailAddress() throws IOException;
}
