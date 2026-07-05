package com.mgr.api.repository.tenant;

import com.mgr.api.model.FitnessExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FitnessExerciseRepository extends JpaRepository<FitnessExercise, Long>, JpaSpecificationExecutor<FitnessExercise> {
}
