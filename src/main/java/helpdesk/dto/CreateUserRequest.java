package helpdesk.dto;

import helpdesk.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private Role role;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String department;
    private String officeNumber;
    private String floorNumber;
    private String telephoneExtension;
    private String building;
}