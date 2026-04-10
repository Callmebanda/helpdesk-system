package helpdesk.dto;

import helpdesk.model.DeviceType;
import helpdesk.model.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TicketResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String department;
    private String officeNumber;
    private String floorNumber;
    private String telephoneExtension;
    private String building;
    private DeviceType deviceType;
    private String problemTitle;
    private String description;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
