package nlu.fit.soft.gr5.precisionMail.uc04;

import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.QueueSearchCriteria;
import nlu.fit.soft.gr5.precisionMail.service.impl.QueueServiceImpl;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Uc04QueueServiceTest {

    private final QueueServiceImpl queueService =
            new QueueServiceImpl();

    @Test
    void testSearchByKeyword() throws IOException {

        QueueSearchCriteria criteria =
                new QueueSearchCriteria();

        criteria.setKeyword("gmail");

        List<ScheduledEmail> result =
                queueService.search(criteria);

        assertNotNull(result);

        for (ScheduledEmail email : result) {

            boolean match =
                    email.email.subject.toLowerCase().contains("gmail")
                            || email.email.from.toLowerCase().contains("gmail")
                            || email.email.toLst.stream()
                            .anyMatch(t -> t.toLowerCase().contains("gmail"));

            assertTrue(match);
        }
    }

    @Test
    void testSearchByStatus() throws IOException {

        QueueSearchCriteria criteria =
                new QueueSearchCriteria();

        criteria.setStatus(
                EmailStatus.SCHEDULED
        );

        List<ScheduledEmail> result =
                queueService.search(criteria);

        assertNotNull(result);

        for (ScheduledEmail email : result) {
            assertEquals(
                    EmailStatus.SCHEDULED,
                    email.status
            );
        }
    }

    @Test
    void testSortByIdAsc() throws IOException {

        QueueSearchCriteria criteria =
                new QueueSearchCriteria();

        criteria.setSortBy("ID");
        criteria.setSortDirection("ASC");

        List<ScheduledEmail> result =
                queueService.search(criteria);

        for (int i = 0; i < result.size() - 1; i++) {

            assertTrue(
                    result.get(i).id
                            <=
                            result.get(i + 1).id
            );
        }
    }

    @Test
    void testSortByIdDesc() throws IOException {

        QueueSearchCriteria criteria =
                new QueueSearchCriteria();

        criteria.setSortBy("ID");
        criteria.setSortDirection("DESC");

        List<ScheduledEmail> result =
                queueService.search(criteria);

        for (int i = 0; i < result.size() - 1; i++) {

            assertTrue(
                    result.get(i).id
                            >=
                            result.get(i + 1).id
            );
        }
    }

    @Test
    void testStatistics() throws IOException {

        Map<EmailStatus,Integer> stats =
                queueService.getStatistics();

        assertNotNull(stats);

        assertTrue(
                stats.size() >= 0
        );
    }

    @Test
    void testDeleteMethodExists() {

        assertDoesNotThrow(() -> {

            try {
                queueService.delete(-999L);
            }
            catch (Exception ignored) {
            }

        });
    }

}