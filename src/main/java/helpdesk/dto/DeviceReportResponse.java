package helpdesk.dto;

import helpdesk.model.DeviceReportStatus;
import helpdesk.model.DeviceReportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeviceReportResponse {

    private Long id;

    private String reportedByUsername;
    private String reportedByFirstName;
    private String reportedByLastName;
    private String reportedByDepartment;

    private Long deviceId;
    private String assetNumber;

    private DeviceReportType reportType;
    private String description;
    private String building;
    private String officeNumber;

    private DeviceReportStatus status;
    private String reviewedByUsername;
    private String reviewNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
}