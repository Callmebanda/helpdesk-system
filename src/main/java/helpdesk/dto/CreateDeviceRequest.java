package helpdesk.dto;

import helpdesk.model.DeviceStatus;
import helpdesk.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDeviceRequest {

    @NotBlank
    private String assetNumber;

    @NotNull
    private DeviceType deviceType;

    private String model;

    private String serialNumber;

    private String building;

    private String officeNumber;

    private DeviceStatus status;

    private String notes;
}