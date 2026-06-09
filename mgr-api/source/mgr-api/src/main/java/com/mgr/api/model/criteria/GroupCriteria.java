package com.mgr.api.model.criteria;

import com.mgr.api.model.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class GroupCriteria implements Serializable {
    private Long id;
    private String name;
    private Integer status;
    private Boolean isSystemRole;
    private String permissionName;
    private Boolean hasNoPermissions;
    private Boolean hasLockedAccount;
    private Boolean hasNoNewsPosters;

    public Specification<Group> getSpecification() {
        return new Specification<Group>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (getIsSystemRole() != null) {
                    predicates.add(cb.equal(root.get("isSystemRole"), getIsSystemRole()));
                }
                if (StringUtils.isNoneBlank(permissionName)) {
                    Join<Group, Permission> permissionJoin = root.join("permission", JoinType.INNER);
                    predicates.add(cb.like(cb.lower(permissionJoin.get("name")), "%" + getPermissionName().trim().toLowerCase() + "%"));
                    query.distinct(true);
                }
                // --- Subquery: tìm Group CHƯA có Permission nào ---
                if (getHasNoPermissions() != null & getHasNoPermissions()) {
                    // Bước 1: Khai báo subquery, kiểu trả về là Permission
                    Subquery<Permission> subquery = query.subquery(Permission.class);

                    // Bước 2: FROM bên trong subquery cũng là Group (correlated subquery)
                    Root<Group> subRoot = subquery.from(Group.class);

                    // Bước 3: JOIN từ Group sang bảng trung gian để đến Permission
                    Join<Group, Permission> subJoin = subRoot.join("permissions");

                    // Bước 4: SELECT và WHERE của subquery
                    // - SELECT: chọn các Permission tồn tại
                    // - WHERE:  chỉ xét Group có ID bằng Group đang xét ở vòng ngoài
                    subquery.select(subJoin)
                            .where(cb.equal(subRoot.get("id"), root.get("id")));

                    // Bước 5: NOT EXISTS(subquery) → Group không có bất kỳ permission nào
                    predicates.add(cb.not(cb.exists(subquery)));
                }
                // --- Subquery: tìm Group account locked ---
                if (hasLockedAccount != null && hasLockedAccount) {
                    Subquery<Account> subquery = query.subquery(Account.class);
                    Root<Group> subroot = subquery.from(Group.class);
                    Join<Group, Account> subJoin = subroot.join("account", JoinType.INNER);
                    subquery.select(subJoin)
                            .where(cb.and(cb.equal(subroot.get("id"), root.get("id")),
                                    cb.equal(subroot.get("status"), 0)));
                    predicates.add(cb.exists(subquery));
                }
//                if (hasNoNewsPosters != null && hasNoNewsPosters) {
//                    Subquery<News> subquery = query.subquery(News.class);
//                    Root<News> subroot = subquery.from(News.class);
//                    Join<News, User> userJoin = subroot.join("user", JoinType.INNER);
//                    Join<User, Account> accountJoin = userJoin.join("account", JoinType.INNER);
//                    subquery.select(subroot)
//                            .where(cb.equal(accountJoin.get("group").get("id"), root.get("id")));
//                    predicates.add(cb.not(cb.exists(subquery)));
//                }
                // --- Subquery: Group user chưa từng có news ---
                if (hasNoNewsPosters != null && hasNoNewsPosters) {
                    Subquery<News> subquery = query.subquery(News.class);
                    Root<News> subroot = subquery.from(News.class);
                    subquery.select(subroot)
                            .where(cb.equal(
                                    subroot.get("user").get("account").get("group").get("id"),
                                    root.get("id")));
                    predicates.add(cb.not(cb.exists(subquery)));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
