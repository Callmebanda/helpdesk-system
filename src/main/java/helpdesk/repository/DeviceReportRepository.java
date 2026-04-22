package helpdesk.repository;

import helpdesk.model.DeviceReport;
import helpdesk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DeviceReportRepository extends JpaRepository<DeviceReport, Long>, JpaSpecificationExecutor<DeviceReport> {

    List<DeviceReport> findByReportedByOrderByCreatedAtDesc(User reportedBy);

    List<DeviceReport> findAllByOrderByCreatedAtDesc();
}