package com.mgr.api.repository;

import com.mgr.api.model.Nation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NationRepository extends JpaRepository<Nation, Long>, JpaSpecificationExecutor<Nation> {
    boolean existsByNameAndParentIsNull(String name);

    boolean existsByNameAndParentId(String name, Long parentId);

    boolean existsByNameAndParentIsNullAndIdIsNot(String name, Long id);

    boolean existsByNameAndParentIdAndIdIsNot(String name, Long parentId, Long id);

    boolean existsByParentId(Long parentId);

    @Modifying
    @Query("DELETE FROM Nation n WHERE n.parent.id = :parentId")
    void deleteByParentId(@Param("parentId") Long parentId);

    @Modifying
    @Query("DELETE FROM Nation n WHERE n.parent.id IN (SELECT p.id FROM Nation p WHERE p.parent.id = :provinceId)")
    void deleteCommunesByProvinceId(@Param("provinceId") Long provinceId);
}
