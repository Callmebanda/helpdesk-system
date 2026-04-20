package helpdesk.service;

import helpdesk.dto.CreateDeviceReportRequest;
import helpdesk.dto.DeviceReportResponse;
import helpdesk.dto.ReviewDeviceReportRequest;
import helpdesk.model.Device;
import helpdesk.model.DeviceReport;
import helpdesk.model.DeviceReportStatus;
import helpdesk.model.User;
import helpdesk.repository.DeviceReportRepository;
import helpdesk.repository.DeviceRepository;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceReportService {

    private final DeviceReportRepository deviceReportRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceReportResponse createReport(String username, CreateDeviceReportRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Device device = null;

        if (request.getDeviceId() != null) {
            device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Device not found"));
        }

        String assetNumber = request.getAssetNumber();

        if ((assetNumber == null || assetNumber.isBlank()) && device != null) {
            assetNumber = device.getAssetNumber();
        }

        DeviceReport report = DeviceReport.builder()
                .reportedBy(user)
                .device(device)
                .assetNumber(assetNumber)
                .reportType(request.getReportType())
                .description(request.getDescription())
                .building(request.getBuilding() != null ? request.getBuilding() : user.getBuilding())
                .officeNumber(request.getOfficeNumber() != null ? request.getOfficeNumber() : user.getOfficeNumber())
                .status(DeviceReportStatus.PENDING)
                .build();

        DeviceReport savedReport = deviceReportRepository.save(report);
        return mapToResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public List<DeviceReportResponse> getMyReports(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return deviceReportRepository.findByReportedByOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceReportResponse> getAllReports() {
        return deviceReportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public DeviceReportResponse reviewReport(Long reportId,
                                             ReviewDeviceReportRequest request,
                                             String reviewerUsername) {
        DeviceReport report = deviceReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Device report not found"));

        report.setStatus(request.getStatus());
        report.setReviewNote(request.getReviewNote());
        report.setReviewedByUsername(reviewerUsername);
        report.setReviewedAt(LocalDateTime.now());

        DeviceReport savedReport = deviceReportRepository.save(report);
        return mapToResponse(savedReport);
    }

    private DeviceReportResponse mapToResponse(DeviceReport report) {
        User user = report.getReportedBy();
        Device device = report.getDevice();

        return DeviceReportResponse.builder()
                .id(report.getId())
                .reportedByUsername(user.getUsername())
                .reportedByFirstName(user.getFirstName())
                .reportedByLastName(user.getLastName())
                .reportedByDepartment(user.getDepartment())
                .deviceId(device != null ? device.getId() : null)
                .assetNumber(report.getAssetNumber())
                .reportType(report.getReportType())
                .description(report.getDescription())
                .building(report.getBuilding())
                .officeNumber(report.getOfficeNumber())
                .status(report.getStatus())
                .reviewedByUsername(report.getReviewedByUsername())
                .reviewNote(report.getReviewNote())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .reviewedAt(report.getReviewedAt())
                .build();
    }
}