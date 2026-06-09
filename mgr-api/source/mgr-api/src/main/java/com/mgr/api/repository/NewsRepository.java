package com.mgr.api.repository;

import com.mgr.api.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    List<News> findAllByCategoryId(Long categoryId);

    List<News> findAllByUserId(Long userId);

    List<News> findAllByStatus(int status);

    List<News> findByCategoryName(String Name);

    @Query("SELECT n FROM News n WHERE n.description LIKE %:keyword%")
    List<News> findByDescriptionContaining(String keyword);

    @Query("SELECT n FROM News n JOIN n.category c WHERE c.name = :categoryName")
    List<News> finCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT n FROM News n " +
            "JOIN FETCH n.user u " +
            "JOIN fetch u.account a "+
            "WHERE n.status = 1")
    List<News> findAllActiveNewsWithAuthorDetail();

    @Query(value = "SELECT * FROM db_mgr_news WHERE thumbnail IS NOT NULL", nativeQuery = true)
    List<News> findNewsWithThumbnailNative();
}