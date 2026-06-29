package com.mgr.api.repository.tenant;

import com.mgr.api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {
    Optional<Tag> findFirstByName(String name);

    Optional<Tag> findFirstBySlug(String slug);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM db_mgr_news_tag WHERE tag_id = :tagId", nativeQuery = true)
    void removeTagFromAllNews(@Param("tagId") Long tagId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM db_mgr_post_tag WHERE tag_id = :tagId", nativeQuery = true)
    void removeTagFromAllPost(@Param("tagId") Long tagId);
}
