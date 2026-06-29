package com.mgr.api.repository.tenant;

import com.mgr.api.model.Nation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NationRepository extends JpaRepository<Nation, Long>, JpaSpecificationExecutor<Nation> {
    boolean existsByNameAndParentIsNull(String name);

    boolean existsByNameAndParentId(String name, Long parentId);

    boolean existsByNameAndParentIsNullAndIdIsNot(String name, Long id);

    boolean existsByNameAndParentIdAndIdIsNot(String name, Long parentId, Long id);

    boolean existsByParentId(Long parentId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Nation n WHERE n.parent.id = :parentId")
    void deleteByParentId(@Param("parentId") Long parentId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM db_mgr_nation WHERE parent_id IN (SELECT temp.id FROM (SELECT id FROM db_mgr_nation WHERE parent_id = :provinceId) AS temp)", nativeQuery = true)
    void deleteCommunesByProvinceId(@Param("provinceId") Long provinceId);
}
