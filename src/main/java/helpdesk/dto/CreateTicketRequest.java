package helpdesk.dto;

import helpdesk.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {

    @NotNull
    private DeviceType deviceType;

    @NotBlank
    private String problemTitle;

    private String description;
}