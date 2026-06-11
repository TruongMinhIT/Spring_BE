package com.mgr.api.model.criteria;

import com.mgr.api.model.Tag;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TagCriteria implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private Integer status;

    public Specification<Tag> getSpecification() {
        return new Specification<Tag>() {
            private static final long seriaVersionUID = 1L;

            @Override
            public @Nullable Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (StringUtils.isNoneBlank(getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().trim().toLowerCase() + "%"));
                }
                if (StringUtils.isNoneBlank(getSlug())) {
                    predicates.add(cb.like(cb.lower(root.get("slug")), "%" + getSlug().trim().toLowerCase() + "%"));
                }
                if (getStatus() != null)
                {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                query.orderBy(cb.desc(root.get("createdDate")));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
