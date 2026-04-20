package helpdesk.dto;

import helpdesk.model.DeviceReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDeviceReportRequest {

    private Long deviceId;

    private String assetNumber;

    @NotNull
    private DeviceReportType reportType;

    @NotBlank
    private String description;

    private String building;

    private String officeNumber;
}