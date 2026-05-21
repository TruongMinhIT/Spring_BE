package com.mgr.api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = TablePrefix.PREFIX_TABLE + "permission")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Permission extends Auditable<String> {
    @Column(name = "name", unique = true)
    private String name;
    private String action;
    private Boolean showMenu;
    private String description;
    private String nameGroup;
    @Column(name = "permission_code")
    private String permissionCode;
}
