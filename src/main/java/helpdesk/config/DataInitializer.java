package helpdesk.config;

import helpdesk.model.Role;
import helpdesk.model.User;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDefaultSupervisor() {
        return args -> {
            if (userRepository.findByUsername("supervisor1").isEmpty()) {
                User supervisor = User.builder()
                        .username("supervisor1")
                        .password(passwordEncoder.encode("ChangeMe123!"))
                        .role(Role.SUPERVISOR)
                        .firstName("System")
                        .lastName("Supervisor")
                        .department("IT")
                        .officeNumber("A-101")
                        .floorNumber("1")
                        .telephoneExtension("1001")
                        .building("Building 1")
                        .enabled(true)
                        .mustChangePassword(true)
                        .build();

                userRepository.save(supervisor);
            }
        };
    }
}