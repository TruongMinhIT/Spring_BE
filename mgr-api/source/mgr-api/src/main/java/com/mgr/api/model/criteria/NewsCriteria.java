package com.mgr.api.model.criteria;

import com.mgr.api.model.Account;
import com.mgr.api.model.News;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class NewsCriteria implements Serializable {
    private String title;
    private String description;
    private Long categoryId;
    private Integer status;
    private Long userId;
    public Specification<News> getSpecification() {
        return new Specification<News>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<News> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (!StringUtils.isEmpty(getTitle())){
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + getTitle().toLowerCase() + "%"));
                }
                if (!StringUtils.isEmpty(getDescription())){
                    predicates.add(cb.like(cb.lower(root.get("description")), "%" + getDescription().toLowerCase() + "%"));
                }
                if (getCategoryId() != null){
                    predicates.add(cb.equal(root.get("category").get("id"),getCategoryId()));
                }
                if (getStatus() != null){
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (getUserId() != null){
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }


}
