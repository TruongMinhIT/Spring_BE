package com.mgr.api.repository;

import com.mgr.api.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {
    Group findFirstByName(String name);

    Optional<Group> findByIdAndIsSystemRole(Long id, Boolean isSystemRole);

    Optional<Group> findByName(String name);
}
