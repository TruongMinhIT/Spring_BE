package com.mgr.api.model.criteria;

import com.mgr.api.model.Account;
import com.mgr.api.model.Category;
import com.mgr.api.model.News;
import com.mgr.api.model.User;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
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
    private String keyword;
    private String categoryName;
    private String authorUsername;

    public Specification<News> getSpecification() {
        return new Specification<News>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<News> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (!StringUtils.isEmpty(getTitle())) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + getTitle().toLowerCase() + "%"));
                }
                if (!StringUtils.isEmpty(getDescription())) {
                    predicates.add(cb.like(cb.lower(root.get("description")), "%" + getDescription().toLowerCase() + "%"));
                }
                if (getCategoryId() != null) {
                    predicates.add(cb.equal(root.get("category").get("id"), getCategoryId()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }
                // Join/ Or
                // Group Or
                if (!StringUtils.isEmpty(getKeyword())) {
                    String searchKey = "%" + getKeyword() + "%";
                    Predicate orPredicate = cb.or(
                            cb.like(cb.lower(root.get("title")), searchKey),
                            cb.like(cb.lower(root.get("description")), searchKey)
                    );
                    predicates.add(orPredicate);
                }
                // Join many to one
                if (StringUtils.isNoneBlank(getCategoryName())) {
                    Join<News, Category> categoryJoin = root.join("category", JoinType.INNER);
                    predicates.add(cb.like(cb.lower(categoryJoin.get("name")), "%" + getCategoryName().trim().toLowerCase() + "%"));
                }
                // Multi Join
                if (StringUtils.isNoneBlank(getAuthorUsername())) {
                    Join<News, User> userJoin = root.join("user", JoinType.INNER);
                    Join<User, Account> accountJoin = userJoin.join("account", JoinType.INNER);
                    predicates.add(cb.like(cb.lower(accountJoin.get("username")), "%" + getAuthorUsername().trim().toLowerCase() + "%"));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}