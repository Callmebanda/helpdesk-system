package helpdesk.dto;

import helpdesk.model.DeviceReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDeviceReportRequest {

    @NotNull
    private DeviceReportStatus status;

    private String reviewNote;
}