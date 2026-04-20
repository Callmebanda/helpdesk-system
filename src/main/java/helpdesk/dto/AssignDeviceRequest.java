package helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignDeviceRequest {

    @NotBlank
    private String username;
}