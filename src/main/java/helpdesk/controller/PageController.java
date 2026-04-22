package helpdesk.controller;

import helpdesk.dto.*;
import helpdesk.model.DeviceType;
import helpdesk.model.IssueCategory;
import helpdesk.model.Role;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.service.*;
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
import org.springframework.web.multipart.MultipartFile;
import helpdesk.dto.KnowledgeArticleResponse;
import helpdesk.dto.UserTicketSummaryResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import helpdesk.dto.AdminTicketFormRequest;
import helpdesk.dto.AssignDeviceRequest;
import helpdesk.dto.CreateDeviceRequest;
import helpdesk.dto.DeviceResponse;
import helpdesk.model.DeviceStatus;
import helpdesk.service.DeviceService;
import helpdesk.dto.CreateDeviceReportRequest;
import helpdesk.dto.DeviceReportResponse;
import helpdesk.dto.ReviewDeviceReportRequest;
import helpdesk.model.DeviceReportStatus;
import helpdesk.model.DeviceReportType;
import helpdesk.service.DeviceReportService;
import org.springframework.data.domain.Page;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final TicketService ticketService;
    private final TicketActivityService ticketActivityService;
    private final UserService userService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final UserImportService userImportService;
    private final DeviceService deviceService;
    private final DeviceReportService deviceReportService;

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
    public String userDashboard(Authentication authentication,
                                Model model,
                                @RequestParam(required = false) TicketStatus status,
                                @RequestParam(required = false) DeviceType deviceType,
                                @RequestParam(required = false) TicketPriority priority,
                                @RequestParam(required = false) Boolean overdue) {
        String username = authentication.getName();

        List<TicketResponse> tickets = ticketService.searchMyTickets(
                username,
                status,
                deviceType,
                priority,
                overdue
        );

        UserTicketSummaryResponse summary = ticketService.getUserTicketSummary(username);

        model.addAttribute("username", username);
        model.addAttribute("tickets", tickets);
        model.addAttribute("summary", summary);

        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("priorities", TicketPriority.values());

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDeviceType", deviceType);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedOverdue", overdue);

        return "user-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication,
                                 Model model,
                                 @RequestParam(required = false) TicketStatus status,
                                 @RequestParam(required = false) DeviceType deviceType,
                                 @RequestParam(required = false) IssueCategory issueCategory,
                                 @RequestParam(required = false) TicketPriority priority,
                                 @RequestParam(required = false) String assignedTechnicianUsername,
                                 @RequestParam(required = false) String department,
                                 @RequestParam(required = false) Boolean overdue,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {

        String username = authentication.getName();
        AdminTicketSummaryResponse summary = ticketService.getTicketSummary();

        Page<AdminTicketResponse> ticketPage = ticketService.searchTicketsPage(
                status,
                deviceType,
                issueCategory,
                priority,
                assignedTechnicianUsername,
                department,
                overdue,
                page,
                size
        );

        model.addAttribute("username", username);
        model.addAttribute("summary", summary);
        model.addAttribute("tickets", ticketPage.getContent());

        model.addAttribute("currentPage", ticketPage.getNumber());
        model.addAttribute("totalPages", ticketPage.getTotalPages());
        model.addAttribute("totalItems", ticketPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", ticketPage.hasPrevious());
        model.addAttribute("hasNext", ticketPage.hasNext());

        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("issueCategories", IssueCategory.values());
        model.addAttribute("priorities", TicketPriority.values());

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDeviceType", deviceType);
        model.addAttribute("selectedIssueCategory", issueCategory);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedAssignedTechnicianUsername", assignedTechnicianUsername);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedOverdue", overdue);

        return "admin-dashboard";
    }
    @GetMapping("/admin/users")
    public String adminUsersPage(Authentication authentication,
                                 Model model,
                                 @RequestParam(required = false) String usernameFilter,
                                 @RequestParam(required = false) Role role,
                                 @RequestParam(required = false) String department,
                                 @RequestParam(required = false) Boolean enabled,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {

        String username = authentication.getName();

        Page<UserResponse> userPage = userService.searchUsersPage(
                usernameFilter,
                role,
                department,
                enabled,
                page,
                size
        );

        model.addAttribute("username", username);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("userForm", new CreateUserRequest());
        model.addAttribute("roles", Role.values());

        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", userPage.hasPrevious());
        model.addAttribute("hasNext", userPage.hasNext());

        model.addAttribute("selectedUsernameFilter", usernameFilter);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedEnabled", enabled);

        return "admin-users";
    }
    @PostMapping("/admin/users/create")
    public String createUserFromPage(@Valid @ModelAttribute("userForm") CreateUserRequest userForm,
                                     BindingResult bindingResult,
                                     Authentication authentication,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", "Please correct the form errors.");
            return "admin-users";
        }

        try {
            userService.createUser(userForm);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin-users";
        }
    }
    @PostMapping("/admin/users/import")
    public String importUsersFromPage(@RequestParam("file") MultipartFile file,
                                      RedirectAttributes redirectAttributes) {
        try {
            UserImportResultResponse result = userImportService.importUsersFromCsv(file);
            String message = "Import complete. Imported: " + result.getImportedCount()
                    + ", Skipped: " + result.getSkippedCount();
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/enable")
    public String enableUserFromPage(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        userService.setUserEnabled(id, true);
        redirectAttributes.addFlashAttribute("successMessage", "User enabled successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/disable")
    public String disableUserFromPage(@PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        userService.setUserEnabled(id, false);
        redirectAttributes.addFlashAttribute("successMessage", "User disabled successfully.");
        return "redirect:/admin/users";
    }

    @GetMapping("/tech/dashboard")
    public String techDashboard(Authentication authentication,
                                Model model,
                                @RequestParam(required = false) TicketStatus status,
                                @RequestParam(required = false) TicketPriority priority,
                                @RequestParam(required = false) Boolean overdue) {
        String username = authentication.getName();

        List<AdminTicketResponse> tickets = ticketService.searchAssignedTickets(
                username,
                status,
                priority,
                overdue
        );

        TechTicketSummaryResponse summary = ticketService.getTechnicianTicketSummary(username);

        model.addAttribute("username", username);
        model.addAttribute("tickets", tickets);
        model.addAttribute("summary", summary);

        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("priorities", TicketPriority.values());

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedOverdue", overdue);

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
                               Model model,
                               RedirectAttributes redirectAttributes) {

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
        redirectAttributes.addFlashAttribute("successMessage", "Ticket submitted successfully.");

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
                                                 Authentication authentication,
                                                 RedirectAttributes redirectAttributes) {
        try {
            ticketService.updateAssignedTicketStatus(id, status, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/tech/tickets/" + id;
    }

    @PostMapping("/tech/tickets/{id}/notes")
    public String updateTechTicketNotesFromPage(@PathVariable Long id,
                                                @RequestParam(required = false) String resolutionNote,
                                                @RequestParam(required = false) String internalNote,
                                                Authentication authentication,
                                                RedirectAttributes redirectAttributes) {
        try {
            UpdateTicketNotesRequest request = new UpdateTicketNotesRequest();
            request.setResolutionNote(resolutionNote);
            request.setInternalNote(internalNote);

            ticketService.updateAssignedTicketNotes(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket notes saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/tech/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/assign")
    public String assignTicketFromPage(@PathVariable Long id,
                                       @RequestParam String technicianUsername,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            AssignTicketRequest request = new AssignTicketRequest();
            request.setTechnicianUsername(technicianUsername);

            ticketService.assignTicket(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket assigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/status")
    public String updateTicketStatusFromPage(@PathVariable Long id,
                                             @RequestParam TicketStatus status,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        try {
            ticketService.updateStatus(id, status, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/priority")
    public String updateTicketPriorityFromPage(@PathVariable Long id,
                                               @RequestParam TicketPriority priority,
                                               Authentication authentication,
                                               RedirectAttributes redirectAttributes) {
        try {
            UpdateTicketPriorityRequest request = new UpdateTicketPriorityRequest();
            request.setPriority(priority);

            ticketService.updatePriority(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket priority updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/admin/tickets/{id}/notes")
    public String updateTicketNotesFromPage(@PathVariable Long id,
                                            @RequestParam(required = false) String resolutionNote,
                                            @RequestParam(required = false) String internalNote,
                                            Authentication authentication,
                                            RedirectAttributes redirectAttributes) {
        try {
            UpdateTicketNotesRequest request = new UpdateTicketNotesRequest();
            request.setResolutionNote(resolutionNote);
            request.setInternalNote(internalNote);

            ticketService.updateNotes(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket notes saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/tickets/" + id;
    }

    @GetMapping("/admin/knowledge")
    public String adminKnowledgePage(Authentication authentication,
                                     Model model,
                                     @RequestParam(required = false) DeviceType deviceType,
                                     @RequestParam(required = false) IssueCategory issueCategory) {
        String username = authentication.getName();
        List<KnowledgeArticleResponse> articles = knowledgeBaseService.getArticles(deviceType, issueCategory);

        model.addAttribute("username", username);
        model.addAttribute("articles", articles);

        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("issueCategories", IssueCategory.values());

        model.addAttribute("selectedDeviceType", deviceType);
        model.addAttribute("selectedIssueCategory", issueCategory);

        return "admin-knowledge";
    }

    @GetMapping("/admin/knowledge/{id}")
    public String adminKnowledgeDetail(@PathVariable Long id,
                                       Authentication authentication,
                                       Model model) {
        String username = authentication.getName();
        KnowledgeArticleResponse article = knowledgeBaseService.getArticleById(id);

        model.addAttribute("username", username);
        model.addAttribute("article", article);

        return "admin-knowledge-detail";
    }

    @PostMapping("/admin/tickets/{id}/publish-knowledge")
    public String publishTicketToKnowledge(@PathVariable Long id,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        try {
            KnowledgeArticleResponse article = knowledgeBaseService.publishFromTicket(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ticket published to knowledge base successfully.");
            return "redirect:/admin/knowledge/" + article.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/tickets/" + id;
        }
    }

    @GetMapping("/admin/tickets/new")
    public String adminNewTicketPage(Authentication authentication, Model model) {
        String username = authentication.getName();

        List<UserResponse> users = userService.getAllUsers().stream()
                .filter(user -> user.getRole() == Role.USER && user.isEnabled())
                .toList();

        model.addAttribute("username", username);
        model.addAttribute("ticketForm", new AdminTicketFormRequest());
        model.addAttribute("users", users);
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("issueCategories", IssueCategory.values());

        return "admin-ticket-form";
    }

    @PostMapping("/admin/tickets/new")
    public String adminSubmitTicket(@Valid @ModelAttribute("ticketForm") AdminTicketFormRequest ticketForm,
                                    BindingResult bindingResult,
                                    Authentication authentication,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<UserResponse> users = userService.getAllUsers().stream()
                    .filter(user -> user.getRole() == Role.USER && user.isEnabled())
                    .toList();

            model.addAttribute("username", authentication.getName());
            model.addAttribute("users", users);
            model.addAttribute("deviceTypes", DeviceType.values());
            model.addAttribute("issueCategories", IssueCategory.values());
            return "admin-ticket-form";
        }

        try {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setDeviceType(ticketForm.getDeviceType());
            request.setIssueCategory(ticketForm.getIssueCategory());
            request.setAssetNumber(ticketForm.getAssetNumber());
            request.setProblemTitle(ticketForm.getProblemTitle());
            request.setDescription(ticketForm.getDescription());
            request.setOtherIssue(ticketForm.getOtherIssue());

            ticketService.createTicketForUser(
                    authentication.getName(),
                    ticketForm.getTargetUsername(),
                    request
            );

            redirectAttributes.addFlashAttribute("successMessage", "Ticket created successfully for user.");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            List<UserResponse> users = userService.getAllUsers().stream()
                    .filter(user -> user.getRole() == Role.USER && user.isEnabled())
                    .toList();

            model.addAttribute("username", authentication.getName());
            model.addAttribute("users", users);
            model.addAttribute("deviceTypes", DeviceType.values());
            model.addAttribute("issueCategories", IssueCategory.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin-ticket-form";
        }
    }

    @GetMapping("/admin/devices")
    public String adminDevicesPage(Authentication authentication,
                                   Model model,
                                   @RequestParam(required = false) String assetNumber,
                                   @RequestParam(required = false) DeviceType deviceType,
                                   @RequestParam(required = false) DeviceStatus status,
                                   @RequestParam(required = false) String assignedUsername,
                                   @RequestParam(required = false) String building,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        String username = authentication.getName();

        Page<DeviceResponse> devicePage = deviceService.searchDevicesPage(
                assetNumber,
                deviceType,
                status,
                assignedUsername,
                building,
                page,
                size
        );

        List<UserResponse> users = userService.getAllUsers().stream()
                .filter(user -> user.isEnabled())
                .toList();

        model.addAttribute("username", username);
        model.addAttribute("devices", devicePage.getContent());
        model.addAttribute("users", users);
        model.addAttribute("deviceForm", new CreateDeviceRequest());
        model.addAttribute("deviceTypes", DeviceType.values());
        model.addAttribute("deviceStatuses", DeviceStatus.values());

        model.addAttribute("currentPage", devicePage.getNumber());
        model.addAttribute("totalPages", devicePage.getTotalPages());
        model.addAttribute("totalItems", devicePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", devicePage.hasPrevious());
        model.addAttribute("hasNext", devicePage.hasNext());

        model.addAttribute("selectedAssetNumber", assetNumber);
        model.addAttribute("selectedDeviceType", deviceType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedAssignedUsername", assignedUsername);
        model.addAttribute("selectedBuilding", building);

        return "admin-devices";
    }

    @PostMapping("/admin/devices/create")
    public String createDeviceFromPage(@Valid @ModelAttribute("deviceForm") CreateDeviceRequest deviceForm,
                                       BindingResult bindingResult,
                                       Authentication authentication,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("devices", deviceService.getAllDevices());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("deviceTypes", DeviceType.values());
            model.addAttribute("deviceStatuses", DeviceStatus.values());
            model.addAttribute("errorMessage", "Please correct the device form errors.");
            return "admin-devices";
        }

        try {
            deviceService.createDevice(deviceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Device created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/devices";
    }

    @PostMapping("/admin/devices/{id}/assign")
    public String assignDeviceFromPage(@PathVariable Long id,
                                       @RequestParam String username,
                                       RedirectAttributes redirectAttributes) {
        try {
            AssignDeviceRequest request = new AssignDeviceRequest();
            request.setUsername(username);

            deviceService.assignDevice(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Device assigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/devices";
    }

    @GetMapping("/user/devices")
    public String userDevicesPage(Authentication authentication, Model model) {
        String username = authentication.getName();

        List<DeviceResponse> devices = deviceService.getMyDevices(username);

        model.addAttribute("username", username);
        model.addAttribute("devices", devices);

        return "user-devices";
    }

    @GetMapping("/user/device-reports")
    public String userDeviceReportsPage(Authentication authentication, Model model) {
        String username = authentication.getName();

        List<DeviceReportResponse> reports = deviceReportService.getMyReports(username);

        model.addAttribute("username", username);
        model.addAttribute("reports", reports);

        return "user-device-reports";
    }

    @GetMapping("/user/device-reports/new")
    public String newUserDeviceReportPage(Authentication authentication, Model model) {
        String username = authentication.getName();

        List<DeviceResponse> devices = deviceService.getMyDevices(username);

        model.addAttribute("username", username);
        model.addAttribute("reportForm", new CreateDeviceReportRequest());
        model.addAttribute("devices", devices);
        model.addAttribute("reportTypes", DeviceReportType.values());

        return "user-device-report-form";
    }

    @PostMapping("/user/device-reports/new")
    public String submitUserDeviceReport(@Valid @ModelAttribute("reportForm") CreateDeviceReportRequest reportForm,
                                         BindingResult bindingResult,
                                         Authentication authentication,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", username);
            model.addAttribute("devices", deviceService.getMyDevices(username));
            model.addAttribute("reportTypes", DeviceReportType.values());
            return "user-device-report-form";
        }

        try {
            deviceReportService.createReport(username, reportForm);
            redirectAttributes.addFlashAttribute("successMessage", "Device report submitted successfully.");
            return "redirect:/user/device-reports";
        } catch (Exception e) {
            model.addAttribute("username", username);
            model.addAttribute("devices", deviceService.getMyDevices(username));
            model.addAttribute("reportTypes", DeviceReportType.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "user-device-report-form";
        }
    }

    @GetMapping("/admin/device-reports")
    public String adminDeviceReportsPage(Authentication authentication, Model model) {
        String username = authentication.getName();

        List<DeviceReportResponse> reports = deviceReportService.getAllReports();

        model.addAttribute("username", username);
        model.addAttribute("reports", reports);
        model.addAttribute("reportStatuses", DeviceReportStatus.values());

        return "admin-device-reports";
    }

    @PostMapping("/admin/device-reports/{id}/review")
    public String reviewDeviceReportFromPage(@PathVariable Long id,
                                             @RequestParam DeviceReportStatus status,
                                             @RequestParam(required = false) String reviewNote,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        try {
            ReviewDeviceReportRequest request = new ReviewDeviceReportRequest();
            request.setStatus(status);
            request.setReviewNote(reviewNote);

            deviceReportService.reviewReport(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Device report reviewed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/device-reports";
    }
}