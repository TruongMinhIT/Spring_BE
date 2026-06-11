package com.mgr.api.model.criteria;

import com.mgr.api.model.Post;
import com.mgr.api.model.Tag;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PostCriteria implements Serializable {
    private Long id;
    private String title;
    private String description;
    private Integer conditionStatus;
    private Boolean isFree;
    private Integer type;
    private Long userId;
    private Long categoryId;
    private List<Long> tagIds;

    public Specification<Post> getSpecification() {
        return new Specification<Post>() {
            private static final long seriaVersionUID = 1L;

            @Override
            public @Nullable Predicate toPredicate(Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (StringUtils.isNoneBlank(getTitle())) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + getTitle().trim().toLowerCase() + "%"));
                }
                if (StringUtils.isNoneBlank(getDescription())) {
                    predicates.add(cb.like(cb.lower(root.get("description")), "%" + getDescription().trim().toLowerCase() + "%"));
                }
                if (getConditionStatus() != null) {
                    predicates.add(cb.equal(root.get("conditionStatus"), getConditionStatus()));
                }
                if (getIsFree() != null){
                    predicates.add(cb.equal(root.get("isFree"), getIsFree()));
                }
                if (getType() != null) {
                    predicates.add(cb.equal(root.get("type"), getType()));
                }
                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }
                if (getCategoryId() != null) {
                    predicates.add(cb.equal(root.get("category").get("id"), getCategoryId()));
                }
                if (getTagIds() != null && !getTagIds().isEmpty()) {
                    Subquery<Post> subquery = query.subquery(Post.class);
                    Root<Post> subroot = subquery.from(Post.class);
                    Join<Post, Tag> subjoin = subroot.join("tags");
                    subquery.select(subroot)
                            .where(subjoin.get("id").in(getTagIds()));
                    predicates.add(root.in(subquery));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
