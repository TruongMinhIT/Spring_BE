package com.mgr.api.model.criteria;

import com.mgr.api.model.Category;
import com.mgr.api.model.News;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryCriteria implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private Boolean hasNews;
    private Boolean hasNewsFromLockedUser;

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
                // --- Subquery: tìm Category có News ---
                if (hasNews != null && hasNews) {
                    Subquery<News> subQuery = criteriaQuery.subquery(News.class);
                    Root<News> subRoot = subQuery.from(News.class);
                    subQuery.select(subRoot)
                            .where(cb.equal(subRoot.get("category").get("id"), root.get("id")));
                    predicates.add(cb.exists(subQuery));
                }
//                if (hasNewsFromLockedUser != null && hasNewsFromLockedUser) {
//                    Subquery<News> subquery = criteriaQuery.subquery(News.class);
//                    Root<News> subroot = subquery.from(News.class);
//                    Join<News, User> userJoin = subroot.join("user", JoinType.INNER);
//                    subquery.select(subroot)
//                            .where(cb.and(cb.equal(userJoin.get("status"), 0),
//                                    cb.equal(subroot.get("category").get("id"), root.get("id"))));
//                    predicates.add(cb.exists(subquery));
//                }
                // --- Subquerry: Category có news từ userlocked ---
                if (hasNewsFromLockedUser != null && hasNewsFromLockedUser) {
                    Subquery<News> subquery = criteriaQuery.subquery(News.class);
                    Root<News> subroot = subquery.from(News.class);
                    subquery.select(subroot)
                            .where(cb.and(cb.equal(subroot.get("user").get("status"), 0),
                                    cb.equal(subroot.get("category").get("id"), root.get("id"))));
                    predicates.add(cb.exists(subquery));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
