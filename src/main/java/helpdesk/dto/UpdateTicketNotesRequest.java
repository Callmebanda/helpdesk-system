package helpdesk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTicketNotesRequest {
    private String resolutionNote;
    private String internalNote;
}