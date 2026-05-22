package com.mgr.api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = TablePrefix.PREFIX_TABLE + "category")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Category  extends Auditable<String>{
    @Column(name = "name", unique = true)
    private String name;
    @Column(name = "description", length = 1000)
    private String description;
}
