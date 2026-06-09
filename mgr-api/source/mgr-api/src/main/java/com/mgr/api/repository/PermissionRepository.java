package com.mgr.api.repository;

import com.mgr.api.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Permission findFirstByName(String name);

    Boolean existsByPermissionCode(String permissionCode);

    Optional<Permission> findByPermissionCode(String permissionCode);

    @Query(value = "SELECT p.* FROM db_mgr_permission p " +
            "INNER JOIN db_mgr_permission_group pg ON p.id = pg.permissionId " +
            "WHERE pg.group_id = :groupId", nativeQuery = true)
    List<Permission> findPermissionByGroupIdNative(@Param("groupId") Long groupId);
}
