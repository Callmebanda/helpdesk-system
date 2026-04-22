package helpdesk.service;

import helpdesk.dto.AssignDeviceRequest;
import helpdesk.dto.CreateDeviceRequest;
import helpdesk.dto.DeviceResponse;
import helpdesk.model.Device;
import helpdesk.model.DeviceStatus;
import helpdesk.model.User;
import helpdesk.repository.DeviceRepository;
import helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import helpdesk.model.DeviceType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceResponse createDevice(CreateDeviceRequest request) {
        if (deviceRepository.existsByAssetNumber(request.getAssetNumber())) {
            throw new RuntimeException("Device asset number already exists");
        }

        Device device = Device.builder()
                .assetNumber(request.getAssetNumber())
                .deviceType(request.getDeviceType())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .building(request.getBuilding())
                .officeNumber(request.getOfficeNumber())
                .status(request.getStatus() != null ? request.getStatus() : DeviceStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        Device savedDevice = deviceRepository.save(device);
        return mapToResponse(savedDevice);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getAllDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getMyDevices(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return deviceRepository.findByAssignedUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public DeviceResponse assignDevice(Long deviceId, AssignDeviceRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        device.setAssignedUser(user);
        device.setBuilding(user.getBuilding());
        device.setOfficeNumber(user.getOfficeNumber());
        device.setStatus(DeviceStatus.ACTIVE);

        Device savedDevice = deviceRepository.save(device);
        return mapToResponse(savedDevice);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> searchDevicesPage(String assetNumber,
                                                  DeviceType deviceType,
                                                  DeviceStatus status,
                                                  String assignedUsername,
                                                  String building,
                                                  int page,
                                                  int size) {

        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 10 : Math.min(size, 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.ASC, "assetNumber")
        );

        Specification<Device> specification = (root, query, criteriaBuilder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            if (assetNumber != null && !assetNumber.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("assetNumber")),
                        "%" + assetNumber.trim().toLowerCase() + "%"
                ));
            }

            if (deviceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("deviceType"), deviceType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (assignedUsername != null && !assignedUsername.isBlank()) {
                Join<Device, User> userJoin = root.join("assignedUser", JoinType.LEFT);

                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(userJoin.get("username")),
                        assignedUsername.trim().toLowerCase()
                ));
            }

            if (building != null && !building.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("building")),
                        "%" + building.trim().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return deviceRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    private DeviceResponse mapToResponse(Device device) {
        User assignedUser = device.getAssignedUser();

        return DeviceResponse.builder()
                .id(device.getId())
                .assetNumber(device.getAssetNumber())
                .deviceType(device.getDeviceType())
                .model(device.getModel())
                .serialNumber(device.getSerialNumber())
                .building(device.getBuilding())
                .officeNumber(device.getOfficeNumber())
                .assignedUsername(assignedUser != null ? assignedUser.getUsername() : null)
                .assignedFirstName(assignedUser != null ? assignedUser.getFirstName() : null)
                .assignedLastName(assignedUser != null ? assignedUser.getLastName() : null)
                .assignedDepartment(assignedUser != null ? assignedUser.getDepartment() : null)
                .status(device.getStatus())
                .notes(device.getNotes())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}