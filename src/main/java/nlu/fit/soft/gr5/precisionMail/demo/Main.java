package nlu.fit.soft.gr5.precisionMail.demo;

public class Main {
    public static void main(String[] args) {
        MailScheduler mailService = new MailScheduler();

        mailService.scheduleEmail(
                "thietvo03@gmail.com",
                "Tiêu đề",
                "Nội dung email tự động",
                600
        );
    }
}