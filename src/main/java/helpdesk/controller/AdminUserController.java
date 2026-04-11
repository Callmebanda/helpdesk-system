package helpdesk.controller;

import helpdesk.dto.CreateUserRequest;
import helpdesk.dto.UserImportResultResponse;
import helpdesk.dto.UserResponse;
import helpdesk.service.UserImportService;
import helpdesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserImportService userImportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}/enable")
    public UserResponse enableUser(@PathVariable Long id) {
        return userService.setUserEnabled(id, true);
    }

    @PatchMapping("/{id}/disable")
    public UserResponse disableUser(@PathVariable Long id) {
        return userService.setUserEnabled(id, false);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserImportResultResponse importUsers(@RequestPart("file") MultipartFile file) {
        return userImportService.importUsersFromCsv(file);
    }
}