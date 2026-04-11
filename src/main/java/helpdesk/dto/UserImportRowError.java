package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserImportRowError {

    private int rowNumber;
    private String username;
    private String message;
}
