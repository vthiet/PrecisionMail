package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.*;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.ConnectionTestProgress;
import nlu.fit.soft.gr5.precisionMail.model.ConnectionTestResult;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EmailServiceImpl implements EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailDao emailDao = new EmailDaoImpl();

    public EmailServiceImpl() { }

    @Override
    public void send(Account account, Email email) throws MessagingException, IOException {
        sendAsync(account, email);
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(Account account, Email email) {
        LOGGER.info(
                "Email send requested. sender={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(account.getUsername()),
                LogHelper.recipientCount(email),
                LogHelper.attachmentCount(email)
        );

        email.status = EmailStatus.SENDING;
        return CompletableFuture.supplyAsync(() -> {
            try {
                EmailUtil.send(account, email);
                email.status = EmailStatus.SENT;
                email.sentAt = java.time.LocalDateTime.now();
                save(email);
                LOGGER.info(
                        "Email sent successfully. sender={}, recipients={}, attachments={}.",
                        LogHelper.maskEmail(account.getUsername()),
                        LogHelper.recipientCount(email),
                        LogHelper.attachmentCount(email)
                );
                return new SendResult(email, true, null);
            } catch (MessagingException | IOException | RuntimeException e) {
                email.status = EmailStatus.FAILED;
                email.errorMessage = e.getMessage();
                try {
                    save(email);
                } catch (IOException saveException) {
                    LOGGER.error("Failed to persist failed email history.", saveException);
                }
                LOGGER.error(
                        "Email send failed. sender={}, recipients={}, attachments={}.",
                        LogHelper.maskEmail(account.getUsername()),
                        LogHelper.recipientCount(email),
                        LogHelper.attachmentCount(email),
                        e
                );
                return new SendResult(email, false, e);
            }
        }, AppExecutors.io());
    }

    @Override
    public ConnectionTestResult validateConnection(Account account) {
        return validateConnection(account, null);
    }

    @Override
    public ConnectionTestResult validateConnection(Account account, Consumer<ConnectionTestProgress> progressListener) {
        ConnectionTestResult result = EmailUtil.validateConnection(account, progressListener);
        if (result.isSuccess()) {
            LOGGER.info("Mail server connection validated for sender={}.", LogHelper.maskEmail(account.getUsername()));
        } else {
            LOGGER.warn(
                    "Mail server connection test failed. sender={}, type={}, step={}.",
                    LogHelper.maskEmail(account.getUsername()),
                    result.type(),
                    result.step(),
                    result.cause()
            );
        }
        return result;
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
