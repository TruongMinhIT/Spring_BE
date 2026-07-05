package com.mgr.api.mapper;

import com.mgr.api.dto.fitnessExercise.FitnessExerciseDto;
import com.mgr.api.form.fitnessExercise.CreateFitnessExerciseForm;
import com.mgr.api.model.FitnessExercise;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FitnessExerciseMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "targetMuscleGroup", target = "targetMuscleGroup")
    @Mapping(source = "difficultyLevel", target = "difficultyLevel")
    @Mapping(source = "instructions", target = "instructions")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateFormToEntity")
    FitnessExercise fromCreateFormToEntity(CreateFitnessExerciseForm createFitnessExerciseForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "targetMuscleGroup", target = "targetMuscleGroup")
    @Mapping(source = "difficultyLevel", target = "difficultyLevel")
    @Mapping(source = "instructions", target = "instructions")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToDto")
    FitnessExerciseDto fromEntityToDto(FitnessExercise fitnessExercise);

    @IterableMapping(elementTargetType = FitnessExerciseDto.class, qualifiedByName = "fromEntityToDto")
    List<FitnessExerciseDto> fromEntityToDtoList(List<FitnessExercise> fitnessExercises);
}
