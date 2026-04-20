package helpdesk.repository;

import helpdesk.model.DeviceReport;
import helpdesk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceReportRepository extends JpaRepository<DeviceReport, Long> {

    List<DeviceReport> findByReportedByOrderByCreatedAtDesc(User reportedBy);

    List<DeviceReport> findAllByOrderByCreatedAtDesc();
}