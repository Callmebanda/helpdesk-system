package helpdesk.service;

import helpdesk.dto.UserImportResultResponse;
import helpdesk.dto.UserImportRowError;
import helpdesk.model.Role;
import helpdesk.model.User;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserImportService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserImportResultResponse importUsersFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("CSV file is required");
        }

        List<UserImportRowError> errors = new ArrayList<>();
        int totalRows = 0;
        int importedCount = 0;
        int skippedCount = 0;

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
                );
                CSVParser csvParser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            validateRequiredHeaders(csvParser.getHeaderMap().keySet());

            for (CSVRecord record : csvParser) {
                totalRows++;
                int rowNumber = (int) record.getRecordNumber() + 1; // +1 to account for header row

                try {
                    String username = getRequiredValue(record, "username");
                    String password = getRequiredValue(record, "password");
                    String firstName = getRequiredValue(record, "firstName");
                    String lastName = getRequiredValue(record, "lastName");

                    if (userRepository.existsByUsername(username)) {
                        skippedCount++;
                        errors.add(UserImportRowError.builder()
                                .rowNumber(rowNumber)
                                .username(username)
                                .message("Username already exists")
                                .build());
                        continue;
                    }

                    String roleValue = getOptionalValue(record, "role");
                    Role role = parseRole(roleValue);

                    User user = User.builder()
                            .username(username)
                            .password(passwordEncoder.encode(password))
                            .role(role)
                            .firstName(firstName)
                            .lastName(lastName)
                            .department(getOptionalValue(record, "department"))
                            .officeNumber(getOptionalValue(record, "officeNumber"))
                            .floorNumber(getOptionalValue(record, "floorNumber"))
                            .telephoneExtension(getOptionalValue(record, "telephoneExtension"))
                            .building(getOptionalValue(record, "building"))
                            .enabled(true)
                            .mustChangePassword(true)
                            .build();

                    userRepository.save(user);
                    importedCount++;

                } catch (Exception e) {
                    skippedCount++;

                    errors.add(UserImportRowError.builder()
                            .rowNumber(rowNumber)
                            .username(safeUsername(record))
                            .message(e.getMessage() != null ? e.getMessage() : "Invalid row data")
                            .build());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file");
        }

        return UserImportResultResponse.builder()
                .totalRows(totalRows)
                .importedCount(importedCount)
                .skippedCount(skippedCount)
                .errors(errors)
                .build();
    }

    private void validateRequiredHeaders(Set<String> headers) {
        List<String> requiredHeaders = List.of("username", "password", "firstName", "lastName");

        for (String header : requiredHeaders) {
            if (!headers.contains(header)) {
                throw new RuntimeException("Missing required CSV header: " + header);
            }
        }
    }

    private String getRequiredValue(CSVRecord record, String column) {
        String value = getOptionalValue(record, column);

        if (value == null || value.isBlank()) {
            throw new RuntimeException(column + " is required");
        }

        return value;
    }

    private String getOptionalValue(CSVRecord record, String column) {
        if (!record.isMapped(column)) {
            return null;
        }

        String value = record.get(column);

        if (value == null) {
            return null;
        }

        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private Role parseRole(String roleValue) {
        if (roleValue == null || roleValue.isBlank()) {
            return Role.USER;
        }

        try {
            return Role.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + roleValue);
        }
    }

    private String safeUsername(CSVRecord record) {
        try {
            return record.isMapped("username") ? record.get("username") : "";
        } catch (Exception e) {
            return "";
        }
    }
}