package helpdesk.service;

import helpdesk.dto.CreateUserRequest;
import helpdesk.dto.UserResponse;
import helpdesk.model.User;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .department(request.getDepartment())
                .officeNumber(request.getOfficeNumber())
                .floorNumber(request.getFloorNumber())
                .telephoneExtension(request.getTelephoneExtension())
                .building(request.getBuilding())
                .enabled(true)
                .mustChangePassword(true)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .department(user.getDepartment())
                .officeNumber(user.getOfficeNumber())
                .floorNumber(user.getFloorNumber())
                .telephoneExtension(user.getTelephoneExtension())
                .building(user.getBuilding())
                .enabled(user.isEnabled())
                .mustChangePassword(user.isMustChangePassword())
                .createdAt(user.getCreatedAt())
                .build();
    }
    public UserResponse setUserEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }
}