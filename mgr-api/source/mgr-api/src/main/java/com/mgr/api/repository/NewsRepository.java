package com.mgr.api.repository;

import com.mgr.api.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    List<News> findAllByCategoryId(Long categoryId);

    List<News> findAllByUserId(Long userId);
}