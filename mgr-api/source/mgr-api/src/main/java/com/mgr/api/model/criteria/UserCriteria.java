package com.mgr.api.model.criteria;

import com.mgr.api.model.Account;
import com.mgr.api.model.Category;
import com.mgr.api.model.User;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserCriteria implements Serializable {
    private Long id;
    private Integer gender;
    private String username;
    private Integer status;
    private String email;
    private String fullName;
    private String phone;

    public Specification<User> getSpecification(){
        return new Specification<User>() {
            private static final long seriaVersionUID = 1L;

            @Override
            public @Nullable Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if(getGender()!=null){
                    predicates.add(cb.equal(root.get("gender"), getGender()));
                }
                if (getStatus() != null ||
                        StringUtils.isNoneBlank(getUsername()) ||
                        StringUtils.isNoneBlank(getEmail()) ||
                        StringUtils.isNoneBlank(getFullName()) ||
                        StringUtils.isNoneBlank(getPhone())) {

                    // Join user to account
                    Join<User, Account> accountJoin = root.join("account", JoinType.INNER);

                    if (getStatus() != null) {
                        predicates.add(cb.equal(accountJoin.get("status"), getStatus()));
                    }
                    if (StringUtils.isNoneBlank(getUsername())) {
                        predicates.add(cb.like(cb.lower(accountJoin.get("username")), "%" + getUsername().trim().toLowerCase() + "%"));
                    }
                    if (StringUtils.isNoneBlank(getEmail())) {
                        predicates.add(cb.like(cb.lower(accountJoin.get("email")), "%" + getEmail().trim().toLowerCase() + "%"));
                    }
                    if (StringUtils.isNoneBlank(getFullName())) {
                        predicates.add(cb.like(cb.lower(accountJoin.get("fullName")), "%" + getFullName().trim().toLowerCase() + "%"));
                    }
                    if (StringUtils.isNoneBlank(getPhone())) {
                        predicates.add(cb.like(accountJoin.get("phone"), "%" + getPhone().trim() + "%"));
                    }
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
