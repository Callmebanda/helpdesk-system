package helpdesk.dto;

import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketFormRequest {

    @NotNull
    private DeviceType deviceType;

    @NotNull
    private IssueCategory issueCategory;

    @NotBlank
    private String assetNumber;

    @NotBlank
    private String problemTitle;

    private String description;

    private String otherIssue;
}