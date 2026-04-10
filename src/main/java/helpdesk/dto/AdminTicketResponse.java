package helpdesk.dto;

import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.model.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminTicketResponse {

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
    private IssueCategory issueCategory;
    private String assetNumber;
    private String problemTitle;
    private String description;
    private String otherIssue;

    private String resolutionNote;
    private String internalNote;

    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    private String assignedTechnicianUsername;
    private String assignedTechnicianFirstName;
    private String assignedTechnicianLastName;
    private LocalDateTime assignedAt;
}