package com.mgr.api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = TablePrefix.PREFIX_TABLE + "user")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User extends Auditable<String>{
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id")
    private Account account;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birth_day")
    private Date dateOfBirth;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DbConfig> dbConfigs = new ArrayList<>();
}
