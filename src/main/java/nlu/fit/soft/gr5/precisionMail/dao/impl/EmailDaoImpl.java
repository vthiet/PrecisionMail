package nlu.fit.soft.gr5.precisionMail.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmailDaoImpl implements EmailDao {
    public String filePath = "emails.json";

    private final ObjectMapper mapper;

    public EmailDaoImpl() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Email save(Email email) throws IOException {
        List<Email> emailLst = findAll();
        emailLst.add(email);

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), emailLst);
        return email;
    }

    @Override
    public List<Email> findAll() throws IOException {
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        try {
            return mapper.readValue(file, new TypeReference<List<Email>>() { });
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    @Override
    public List<String> findAllEmailAddress() throws IOException {
        return findAll().stream().map(Email::getFrom).toList();
    }
}
