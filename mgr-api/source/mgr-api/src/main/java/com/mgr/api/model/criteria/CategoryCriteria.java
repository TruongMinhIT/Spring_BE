package com.mgr.api.model.criteria;

import com.mgr.api.model.Category;
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
public class CategoryCriteria implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Integer status;

    public Specification<Category> getSpecification(){
        return new Specification<Category>() {
            private static final long seriaVersionUID = 1L;

            @Override
            public @Nullable Predicate toPredicate(Root<Category> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null){
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getName() != null){
                    predicates.add(cb.like(cb.lower(root.get("name")), "%"+ getName().toLowerCase()+"%"));
                }
                if (getDescription()!=null){
                    predicates.add(cb.like(cb.lower(root.get("description")), "%" + getDescription() +"%"));
                }
                if (getStatus()!=null){
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
