package helpdesk.dto;

import helpdesk.model.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private Role role;
    private String firstName;
    private String lastName;
    private String department;
    private String officeNumber;
    private String floorNumber;
    private String telephoneExtension;
    private String building;
    private boolean enabled;
    private boolean mustChangePassword;
    private LocalDateTime createdAt;
}