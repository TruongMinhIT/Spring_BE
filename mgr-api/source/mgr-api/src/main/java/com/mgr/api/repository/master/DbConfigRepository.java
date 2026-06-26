package com.mgr.api.repository.master;

import com.mgr.api.model.DbConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DbConfigRepository extends JpaRepository<DbConfig, Long>, JpaSpecificationExecutor<DbConfig> {
    Boolean existsByName(String name);

    Boolean existsByNameAndIdNot(String name, Long id);
}
