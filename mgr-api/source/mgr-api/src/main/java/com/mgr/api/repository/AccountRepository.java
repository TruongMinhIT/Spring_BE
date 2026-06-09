package com.mgr.api.repository;

import com.mgr.api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findFirstByUsername(String username);

    Optional<Account> findFirstByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByUsernameAndIdNot(String username, Long id);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndIdNot(String email, Long id);

    Boolean existsByPhone(String phone);

    Boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<Account> findFirstByPhone(String phone);

    @Query("SELECT a FROM Account a WHERE a.username = :username OR a.phone = :phone")
    Optional<Account> findByUsernameOrPhone(@Param("username") String username, @Param("phone") String phone);

    Optional<Account> findByIdAndStatus(long id, Integer status);

    List<Account> findByKindOrIsSuperAdminTrue(int kind);

    List<Account> findByLastLoginGreaterThan(Date lastLogin);

    List<Account> findByGroupNameContaining(String groupName);

    @Query("SELECT a FROM Account a WHERE a.email = :email")
    Optional<Account> findByEmail(@Param("email") String email);

    @Query("SELECT a FROM Account a WHERE a.status = 0 OR a.attemptLogin > :maxAttemp")
    List<Account> findLockedOrExcessiveLoginAttempt(@Param("maxAttempt") Integer maxAttempt);

    @Query("SELECT a FROM Account a JOIN a.group g WHERE g.kind = 2")
    List<Account> findByGroupKind();

    @Query(value = "SELECT * FROM db_mgr_account WHERE username = :username", nativeQuery = true)
    Optional<Account> findByUsername(@Param("username") String username);
}
