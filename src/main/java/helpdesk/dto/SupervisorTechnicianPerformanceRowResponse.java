package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupervisorTechnicianPerformanceRowResponse {

    private String technicianUsername;
    private String technicianName;

    private long totalAssigned;
    private long pending;
    private long inProgress;
    private long resolved;
    private long escalated;
    private long overdue;

    private Double averageResolutionHours;
}