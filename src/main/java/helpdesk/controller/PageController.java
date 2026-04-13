package helpdesk.controller;

import helpdesk.dto.AdminTicketResponse;
import helpdesk.dto.AdminTicketSummaryResponse;
import helpdesk.dto.AssignTicketRequest;
import helpdesk.dto.CreateTicketRequest;
import helpdesk.dto.TicketActivityResponse;
import helpdesk.dto.TicketFormRequest;
import helpdesk.dto.TicketResponse;
import helpdesk.dto.UpdateTicketNotesRequest;
import helpdesk.dto.UpdateTicketPriorityRequest;
import helpdesk.dto.UserResponse;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.model.Role;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.service.TicketActivityService;
import helpdesk.service.TicketService;
import helpdesk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final TicketService ticketService;
    private final TicketActivityService ticketActivityService;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean isSupervisor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPERVISOR"));

        boolean isTechnician = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TECHNICIAN"));

        if (isSupervisor) {
            return "redirect:/admin/dashboard";
        }

        if (isTechnician) {
            return "redirect:/tech/dashboard";
        }

        return "redirect:/user/dashboard";
    }

    @GetMapping("/user/dashboard")
    public String userDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<TicketResponse> tickets = ticketService.getMyTickets(username);

        model.addAttribute("username", username);
        model.addAttribute("tickets", tickets);

        return "user-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        AdminTicketSummaryResponse summary = ticketService.getTicketSummary();
        List<AdminTicketResponse> tickets = ticketService.getAllTickets();

        model.addAttribute("username", username);
        model.addAttribute("summary", summary);
        model.addAttribute("tickets", tickets);

        return "admin-dashboard";
    }

    @GetMapping("/tech/dashboard")
    public String techDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<AdminTicketResponse> tickets = ticketService.getAssignedTickets(username);

        model.addAttribute("username", username);
        model.addAttribute("tickets", tickets);

        return "tech-dashboard";
    }

    @GetMapping("/user/tickets/new")
    public String newTicketPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("ticketForm", new TicketFormRequest());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("issueCategories", IssueCategory.values());

        return "user-ticket-form";
    }

    @PostMapping("/user/tickets/new")
    public String submitTicket(@Valid @ModelAttribute("ticketForm") TicketFormRequest ticketForm,
                               BindingResult bindingResult,
                               Authentication authentication,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("deviceTypes", DeviceType.values());
            model.addAttribute("issueCategories", IssueCategory.values());
            return "user-ticket-form";
        }

        CreateTicketRequest request = new CreateTicketRequest();
        request.setDeviceType(ticketForm.getDeviceType());
        request.setIssueCategory(ticketForm.getIssueCategory());
        request.setAssetNumber(ticketForm.getAssetNumber());
        request.setProblemTitle(ticketForm.getProblemTitle());
        request.setDescription(ticketForm.getDescription());
        request.setOtherIssue(ticketForm.getOtherIssue());

        ticketService.createTicket(authentication.getName(), request);

        return "redirect:/user/dashboard";
    }

    @GetMapping("/user/tickets/{id}")
    public String userTicketDetail(@PathVariable Long id,
                                   Authentication authentication,
                                   Model model) {
        String username = authentication.getName();

        TicketResponse ticket = ticketService.getMyTicketById(id, username);
        List<TicketActivityResponse> activities =
                ticketActivityService.getUserVisibleActivities(id, username);

        model.addAttribute("username", username);
        model.addAttribute("ticket", ticket);
        model.addAttribute("activities", activities);

        return "user-ticket-detail";
    }

    @GetMapping("/admin/tickets/{id}")
    public String adminTicketDetail(@PathVariable Long id,
                                    Authentication authentication,
                                    Model model) {
        String username = authentication.getName();

        AdminTicketResponse ticket = ticketService.getTicketByIdForAdmin(id);
        List<TicketActivityResponse> activities = ticketActivityService.getAdminActivities(id);
        List<UserResponse> technicians = userService.getAllUsers().stream()
                .filter(user -> user.getRole() == Role.TECHNICIAN && user.isEnabled())
                .toList();

        model.addAttribute("username", username);
        model.addAttribute("ticket", ticket);
        model.addAttribute("activities", activities);
        model.addAttribute("technicians", technicians);
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("priorities", TicketPriority.values());

        return "admin-ticket-detail";
    }

    @GetMapping("/tech/tickets/{id}")
    public String techTicketDetail(@PathVariable Long id,
                                   Authentication authentication,
                                   Model model) {
        String username = authentication.getName();

        AdminTicketResponse ticket = ticketService.getAssignedTicketById(id, username);
        List<TicketActivityResponse> activities =
                ticketActivityService.getTechnicianActivities(id, username);

        model.addAttribute("username", username);
        model.addAttribute("ticket", ticket);
        model.addAttribute("activities", activities);
        model.addAttribute("statuses", TicketStatus.values());

        return "tech-ticket-detail";
    }

    @PostMapping("/tech/tickets/{id}/status")
    public String updateTechTicketStatusFromPage(@PathVariable Long id,
                                                 @RequestParam TicketStatus status,
                                                 Authentication authentication) {
        ticketService.updateAssignedTicketStatus(id, status, authentication.getName());
        return "redirect:/tech/tickets/" + id;
    }

    @PostMapping("/tech/tickets/{id}/notes")
    public String updateTechTicketNotesFromPage(@PathVariable Long id,
                                                @RequestParam(required = false) String resolutionNote,
                                                @RequestParam(required = false) String internalNote,
                                                Authentication authentication) {
        UpdateTicketNotesRequest request = new UpdateTicketNotesRequest();
        request.setResolutionNote(resolutionNote);
        request.setInternalNote(internalNote);

        ticketService.updateAssignedTicketNotes(id, request, authentication.getName());
        return "redirect:/tech/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/assign")
    public String assignTicketFromPage(@PathVariable Long id,
                                       @RequestParam String technicianUsername,
                                       Authentication authentication) {
        AssignTicketRequest request = new AssignTicketRequest();
        request.setTechnicianUsername(technicianUsername);

        ticketService.assignTicket(id, request, authentication.getName());
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/status")
    public String updateTicketStatusFromPage(@PathVariable Long id,
                                             @RequestParam TicketStatus status,
                                             Authentication authentication) {
        ticketService.updateStatus(id, status, authentication.getName());
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/priority")
    public String updateTicketPriorityFromPage(@PathVariable Long id,
                                               @RequestParam TicketPriority priority,
                                               Authentication authentication) {
        UpdateTicketPriorityRequest request = new UpdateTicketPriorityRequest();
        request.setPriority(priority);

        ticketService.updatePriority(id, request, authentication.getName());
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/notes")
    public String updateTicketNotesFromPage(@PathVariable Long id,
                                            @RequestParam(required = false) String resolutionNote,
                                            @RequestParam(required = false) String internalNote,
                                            Authentication authentication) {
        UpdateTicketNotesRequest request = new UpdateTicketNotesRequest();
        request.setResolutionNote(resolutionNote);
        request.setInternalNote(internalNote);

        ticketService.updateNotes(id, request, authentication.getName());
        return "redirect:/admin/tickets/" + id;
    }
}