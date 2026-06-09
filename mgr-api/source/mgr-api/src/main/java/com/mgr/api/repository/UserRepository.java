package com.mgr.api.repository;

import com.mgr.api.model.User;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    List<User> findByGenderAndDateOfBirthBetween(Integer gender, Date dateStart, Date dateEnd);

    Optional<User> findByAccountPhone(String phone);

    @Query("SELECT u FROM User u " +
            "JOIN u.account a " +
            "JOIN a.group g " +
            "JOIN g.permissions p " +
            "WHERE p.permissionCode = :permissionCode")
    List<User> findByPermissionCode(@Param("permissionCode") String permissionCode);

    @Query(value = "SELECT gender, COUNT (id) FROM db_mgr_user GROUP BY gender", nativeQuery = true)
    List<Object[]> countUserByGender();
}
