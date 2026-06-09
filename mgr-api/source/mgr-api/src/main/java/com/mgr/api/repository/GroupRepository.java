package com.mgr.api.repository;

import com.mgr.api.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {
    Group findFirstByName(String name);

    Optional<Group> findByIdAndIsSystemRole(Long id, Boolean isSystemRole);

    Optional<Group> findByName(String name);

    List<Group> findAllByIsSystemRoleFalse();

    @Query("SELECT g FROM Group g WHERE g.status = 1 AND g.isSystemRole = false")
    List<Group> findActiveNonSystemGroups( );
}
