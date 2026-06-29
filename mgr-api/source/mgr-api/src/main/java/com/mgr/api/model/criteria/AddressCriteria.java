package com.mgr.api.model.criteria;

import com.mgr.api.model.Address;
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
public class AddressCriteria implements Serializable {
    private Long id;
    private Long userId;
    private Long provinceId;
    private Long districtId;
    private Long communeId;
    private Boolean isDefault;

    public Specification<Address> getSpecification() {
        return new Specification<Address>() {
            private static final long serialVersionUID = 1L;

            @Override
            public @Nullable Predicate toPredicate(Root<Address> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), getUserId()));
                }
                if (getProvinceId() != null) {
                    predicates.add(cb.equal(root.get("province").get("id"), getProvinceId()));
                }
                if (getDistrictId() != null) {
                    predicates.add(cb.equal(root.get("district").get("id"), getDistrictId()));
                }
                if (getCommuneId() != null) {
                    predicates.add(cb.equal(root.get("commune").get("id"), getCommuneId()));
                }
                if (getIsDefault() != null) {
                    predicates.add(cb.equal(root.get("isDefault"), getIsDefault()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
