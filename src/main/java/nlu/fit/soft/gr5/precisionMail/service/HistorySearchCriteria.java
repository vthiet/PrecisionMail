package nlu.fit.soft.gr5.precisionMail.service;

import java.time.LocalDate;

public record HistorySearchCriteria(String keyword, LocalDate startDate, LocalDate endDate) {
    public String normalizedKeyword() {
        return keyword == null ? "" : keyword.trim();
    }
}
