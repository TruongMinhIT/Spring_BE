package com.mgr.api.repository;

import com.mgr.api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findFirstByName(String name);

    List<Category> findAllByNameContainingAndStatus(String keyword, Integer status);

    @Query(value = "SELECT c.name, COUNT(n.id )" +
            "FROM db_mgr_category c " +
            "LEFT JOIN db_mgr_news n ON c.id = n.category_id " +
            "GROUP BY c.id, c.name", nativeQuery = true)
    List<Object[]> countNewsPerCategory();
}
