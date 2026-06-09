package com.mgr.api.model.criteria;


import com.mgr.api.model.Account;
import com.mgr.api.model.News;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AccountCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private int kind;
    private String username;
    private Integer status;
    private String email;
    private String fullName;
    private String phone;
    private List<Integer> kinds;
    private Date lastLoginAfter;
    private Boolean neverLoggedIn;
    private Integer minAttemptLogin;
    private String keyWord;
    private Boolean hasNeverPostedNews;

    public Specification<Account> getSpecification() {
        return new Specification<Account>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getKind() > 0) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (!StringUtils.isEmpty(getUsername())) {
                    predicates.add(cb.like(cb.lower(root.get("username")), "%" + getUsername().toLowerCase() + "%"));
                }
                if (!StringUtils.isEmpty(getEmail())) {
                    predicates.add(cb.like(cb.lower(root.get("email")), "%" + getEmail().toLowerCase() + "%"));
                }
                if (!StringUtils.isEmpty(getFullName())) {
                    predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + getFullName().toLowerCase() + "%"));
                }
                if (!StringUtils.isEmpty(getPhone())) {
                    predicates.add(cb.like(root.get("phone"), "%" + getPhone() + "%"));
                }
                // Nang cao
                if (getKinds() != null && !getKinds().isEmpty()) {
                    predicates.add(root.get("kind").in(getKinds()));
                }
                if (getLastLoginAfter() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("lastLogin"), getLastLoginAfter()));
                }
                if (neverLoggedIn != null && getNeverLoggedIn()) {
                    predicates.add(cb.isNull(root.get("lastLogin")));
                }
                if (minAttemptLogin != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("attemptLogin"), getMinAttemptLogin()));
                }
                if (!StringUtils.isEmpty(getKeyWord())) {
                    String keywordLike = "%" + getKeyWord().trim().toLowerCase() + "%";
                    Predicate orPredicate = cb.or(
                            cb.like(cb.lower(root.get("username")), keywordLike),
                            cb.like(cb.lower(root.get("email")), keywordLike),
                            cb.like(cb.lower(root.get("fullname")), keywordLike)
                    );
                    predicates.add(orPredicate);
                }
                // SubQuerry has never posted news
                if (hasNeverPostedNews != null && hasNeverPostedNews) {
                    Subquery<News> subquery = query.subquery(News.class);
                    Root<News> subRoot = subquery.from(News.class);
                    subquery.select(subRoot)
                            .where(cb.equal(subRoot.get("user").get("id"), root.get("id")));
                    predicates.add(cb.not(cb.exists(subquery)));
                }

                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }

}
