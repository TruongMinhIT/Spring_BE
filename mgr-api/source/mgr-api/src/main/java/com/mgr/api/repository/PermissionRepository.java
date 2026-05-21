package com.mgr.api.repository;

import com.mgr.api.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Permission findFirstByName(String name);

    Boolean existsByPermissionCode(String permissionCode);
}
