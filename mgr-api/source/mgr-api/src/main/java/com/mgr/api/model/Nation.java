package com.mgr.api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = TablePrefix.PREFIX_TABLE + "nation")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Nation extends Auditable<String> {
    @Column(name = "name")
    private String name;

    @Column(name = "kind")
    private Integer kind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Nation parent;
}
