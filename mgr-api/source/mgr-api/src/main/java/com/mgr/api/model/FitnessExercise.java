package com.mgr.api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = TablePrefix.PREFIX_TABLE + "fitness_exercise")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class FitnessExercise extends Auditable<String> {
    @Column(name = "name")
    private String name;

    @Column(name = "target_muscle_group")
    private String targetMuscleGroup;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;
}
