package helpdesk.repository;

import helpdesk.model.Device;
import helpdesk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

    boolean existsByAssetNumber(String assetNumber);

    Optional<Device> findByAssetNumber(String assetNumber);

    List<Device> findByAssignedUser(User assignedUser);
}