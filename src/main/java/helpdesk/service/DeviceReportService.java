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
import helpdesk.model.DeviceReportType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

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

    @Transactional(readOnly = true)
    public Page<DeviceReportResponse> searchReportsPage(DeviceReportStatus status,
                                                        DeviceReportType reportType,
                                                        String username,
                                                        String assetNumber,
                                                        String department,
                                                        int page,
                                                        int size) {

        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 10 : Math.min(size, 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<DeviceReport> specification = (root, query, criteriaBuilder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (reportType != null) {
                predicates.add(criteriaBuilder.equal(root.get("reportType"), reportType));
            }

            if (assetNumber != null && !assetNumber.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("assetNumber")),
                        "%" + assetNumber.trim().toLowerCase() + "%"
                ));
            }

            if ((username != null && !username.isBlank()) || (department != null && !department.isBlank())) {
                Join<DeviceReport, User> userJoin = root.join("reportedBy", JoinType.LEFT);

                if (username != null && !username.isBlank()) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(userJoin.get("username")),
                            "%" + username.trim().toLowerCase() + "%"
                    ));
                }

                if (department != null && !department.isBlank()) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(userJoin.get("department")),
                            "%" + department.trim().toLowerCase() + "%"
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return deviceReportRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
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