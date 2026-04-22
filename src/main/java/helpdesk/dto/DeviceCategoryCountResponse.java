package helpdesk.dto;

import helpdesk.model.DeviceType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeviceCategoryCountResponse {

    private DeviceType deviceType;
    private long totalDevices;
    private long assignedDevices;
    private long unassignedDevices;
}
