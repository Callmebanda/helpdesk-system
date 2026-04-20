package helpdesk.dto;

import helpdesk.model.DeviceStatus;
import helpdesk.model.DeviceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeviceResponse {

    private Long id;
    private String assetNumber;
    private DeviceType deviceType;
    private String model;
    private String serialNumber;
    private String building;
    private String officeNumber;

    private String assignedUsername;
    private String assignedFirstName;
    private String assignedLastName;
    private String assignedDepartment;

    private DeviceStatus status;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}