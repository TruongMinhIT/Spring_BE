package com.mgr.api.repository.tenant;

import com.mgr.api.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {
    Boolean existsByUserId(Long userId);

    List<Address> findAllByUserIdOrderByIsDefaultDesc(Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultAddressByUserId(@Param("userId") Long userId);

    boolean existsByProvinceIdOrDistrictIdOrCommuneId(Long provinceId, Long districtId, Long communeId);
}
