package com.mgr.api.repository.tenant;

import com.mgr.api.dto.user.UserExportRow;
import com.mgr.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Tenant-scoped user repository (bound to {@code tenantEntityManagerFactory} via the
 * {@code repository.tenant} package). Queries run on the tenant-routed persistence unit, so the
 * results are automatically scoped to the caller's current tenant ({@code TenantContext}); no
 * manual tenant predicate is needed. Distinct from the master-scoped
 * {@code repository.master.UserRepository} used by the CRUD endpoints.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Projection of all users of the current tenant into non-sensitive export rows, ordered by
     * creation date. Uses a JPA constructor expression selecting only the five exported columns,
     * so no entity (and no sensitive column) is materialized (FR-005).
     */
    @Query("select new com.mgr.api.dto.user.UserExportRow("
            + "a.fullName, a.email, g.name, a.status, a.createdDate) "
            + "from User u join u.account a left join a.group g "
            + "order by a.createdDate")
    List<UserExportRow> findAllForExport();

    /**
     * Same projection filtered to a single account status code (FR-006).
     */
    @Query("select new com.mgr.api.dto.user.UserExportRow("
            + "a.fullName, a.email, g.name, a.status, a.createdDate) "
            + "from User u join u.account a left join a.group g "
            + "where a.status = :statusCode "
            + "order by a.createdDate")
    List<UserExportRow> findAllForExportByStatus(@Param("statusCode") int statusCode);
}
