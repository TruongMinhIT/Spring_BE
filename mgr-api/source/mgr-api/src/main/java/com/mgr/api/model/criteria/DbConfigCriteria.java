package com.mgr.api.model.criteria;

import com.mgr.api.model.DbConfig;
import com.mgr.api.model.Nation;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class DbConfigCriteria {
    private Long id;
    private String name;
    private String username;
    private Long userId;


    public Specification<DbConfig> getSpecification() {
        return new Specification<DbConfig>() {
            private static final long serialVersionUID = 1L;

            @Override
            public @Nullable Predicate toPredicate(Root<DbConfig> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (StringUtils.isNoneBlank(getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().trim().toLowerCase() + "%"));
                }
                if (StringUtils.isNotBlank(getUsername())) {
                    predicates.add(cb.like(cb.lower(root.get("username")), "%" + getUsername().trim().toLowerCase() + "%"));
                }
                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
