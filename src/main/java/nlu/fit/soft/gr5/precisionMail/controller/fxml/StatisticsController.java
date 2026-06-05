package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import nlu.fit.soft.gr5.precisionMail.dao.ScheduledEmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.ScheduledEmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;

import java.util.Map;

public class StatisticsController {
    @FXML
    private PieChart statusChart;
    private final ScheduledEmailDao dao = new ScheduledEmailDaoImpl();

    @FXML
    public void initialize() {
        loadData();
    }

    private String getVietnameseLabel(EmailStatus status) {
        return switch (status) {
            case SENT -> "Đã gửi";
            case MISSED -> "Bỏ lỡ";
            case CANCELLED -> "Đã hủy";
            case SENDING -> "Đang gửi";
            case RETRY_PENDING -> "Chờ gửi lại";
            case FAILED -> "Thất bại";
            default -> status.name();
        };
    }

    private void loadData() {
        Map<EmailStatus, Long> stats = dao.countByStatus();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        stats.forEach((status, count) -> {
            String label = getVietnameseLabel(status) + " (" + count + ")";
            pieChartData.add(new PieChart.Data(label, count));
        });

        statusChart.setData(pieChartData);
    }
}