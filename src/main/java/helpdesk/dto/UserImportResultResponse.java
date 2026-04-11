package helpdesk.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserImportResultResponse {

    private int totalRows;
    private int importedCount;
    private int skippedCount;
    private List<UserImportRowError> errors;
}