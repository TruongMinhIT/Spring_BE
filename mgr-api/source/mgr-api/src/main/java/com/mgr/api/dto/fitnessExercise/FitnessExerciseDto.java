package com.mgr.api.dto.fitnessExercise;

import com.mgr.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FitnessExerciseDto extends ABasicAdminDto {
    @ApiModelProperty(name = "name")
    private String name;
    @ApiModelProperty(name = "targetMuscleGroup")
    private String targetMuscleGroup;
    @ApiModelProperty(name = "difficultyLevel")
    private Integer difficultyLevel;
    @ApiModelProperty(name = "instructions")
    private String instructions;
}
