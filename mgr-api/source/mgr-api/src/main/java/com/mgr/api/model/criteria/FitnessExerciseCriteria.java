package com.mgr.api.model.criteria;

import com.mgr.api.model.FitnessExercise;
import lombok.Data;
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
public class FitnessExerciseCriteria implements Serializable {
    private String name;
    private Integer status;

    public Specification<FitnessExercise> getSpecification() {
        return new Specification<FitnessExercise>() {
            private static final long seriaVersionUID = 1L;

            @Override
            public @Nullable Predicate toPredicate(Root<FitnessExercise> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
