package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.*;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailServiceImpl implements EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "email-send-worker");
        thread.setDaemon(true);
        return thread;
    });

    private final EmailDao emailDao = new EmailDaoImpl();

    public EmailServiceImpl() { }

    @Override
    public void send(Account account, Email email) throws MessagingException, IOException {
        LOGGER.info(
                "Email send requested. sender={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(account.getUsername()),
                LogHelper.recipientCount(email),
                LogHelper.attachmentCount(email)
        );

        EMAIL_EXECUTOR.submit(() -> {
            try {
                EmailUtil.send(account, email);
                save(email);
                LOGGER.info(
                        "Email sent successfully. sender={}, recipients={}, attachments={}.",
                        LogHelper.maskEmail(account.getUsername()),
                        LogHelper.recipientCount(email),
                        LogHelper.attachmentCount(email)
                );
            } catch (MessagingException | IOException | RuntimeException e) {
                LOGGER.error(
                        "Email send failed. sender={}, recipients={}, attachments={}.",
                        LogHelper.maskEmail(account.getUsername()),
                        LogHelper.recipientCount(email),
                        LogHelper.attachmentCount(email),
                        e
                );
            }
        });
    }

    @Override
    public List<Email> findAll() throws IOException {
        return emailDao.findAll();
    }

    @Override
    public Email save(Email email) throws IOException {
        return emailDao.save(email);
    }
}
