package com.mgr.api.form.fitnessExercise;

import com.mgr.api.validation.ValidDifficultyLevel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel
public class UpdateFitnessExerciseForm {
    @NotNull(message = "id can not be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;

    @NotEmpty(message = "name can not be empty")
    @Size(max = 255, message = "name must not exceed 255 characters")
    @ApiModelProperty(name = "name", required = true)
    private String name;

    @NotEmpty(message = "targetMuscleGroup can not be empty")
    @Size(max = 255, message = "targetMuscleGroup must not exceed 255 characters")
    @ApiModelProperty(name = "targetMuscleGroup", required = true)
    private String targetMuscleGroup;

    @NotNull(message = "difficultyLevel can not be null")
    @ValidDifficultyLevel
    @ApiModelProperty(name = "difficultyLevel", required = true)
    private Integer difficultyLevel;

    @NotEmpty(message = "instructions can not be empty")
    @Size(max = 2000, message = "instructions must not exceed 2000 characters")
    @ApiModelProperty(name = "instructions", required = true)
    private String instructions;
}
