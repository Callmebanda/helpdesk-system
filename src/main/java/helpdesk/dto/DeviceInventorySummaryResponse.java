package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DeviceInventorySummaryResponse {

    private long totalDevices;
    private long assignedDevices;
    private long unassignedDevices;

    private List<DeviceCategoryCountResponse> categoryCounts;
}