package helpdesk.controller;

import helpdesk.dto.ChangePasswordRequest;
import helpdesk.dto.UserResponse;
import helpdesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMyProfile(Principal principal) {
        return userService.getUserByUsername(principal.getName());
    }

    @PatchMapping("/change-password")
    public Map<String, String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                              Principal principal) {
        userService.changePassword(
                principal.getName(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return Map.of("message", "Password changed successfully");
    }
}