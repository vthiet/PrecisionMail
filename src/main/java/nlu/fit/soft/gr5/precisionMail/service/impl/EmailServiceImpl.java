package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.util.AppLoaderUtil;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;

import java.util.List;
import java.util.Properties;

public class EmailServiceImpl implements EmailService {

    private final EmailDao emailRepository = new EmailDaoImpl();

    public EmailServiceImpl() { }

    @Override
    public void send(Account account, Email email) throws MessagingException {
        EmailUtil.send(account, email);
    }
}