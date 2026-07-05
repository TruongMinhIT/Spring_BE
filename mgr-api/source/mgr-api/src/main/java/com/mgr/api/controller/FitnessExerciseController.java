package com.mgr.api.controller;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.fitnessExercise.FitnessExerciseDto;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.fitnessExercise.CreateFitnessExerciseForm;
import com.mgr.api.form.fitnessExercise.UpdateFitnessExerciseForm;
import com.mgr.api.mapper.FitnessExerciseMapper;
import com.mgr.api.model.FitnessExercise;
import com.mgr.api.model.criteria.FitnessExerciseCriteria;
import com.mgr.api.repository.tenant.FitnessExerciseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/fitness-exercise")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class FitnessExerciseController extends ABasicController {
    @Autowired
    FitnessExerciseRepository fitnessExerciseRepository;

    @Autowired
    FitnessExerciseMapper fitnessExerciseMapper;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EXERCISE_V')")
    public ApiMessageDto<FitnessExerciseDto> getFitnessExercise(@PathVariable("id") Long id) {
        ApiMessageDto<FitnessExerciseDto> apiMessageDto = new ApiMessageDto<>();
        FitnessExercise fitnessExercise = fitnessExerciseRepository.findById(id).orElse(null);

        if (fitnessExercise == null) {
            throw new NotFoundException("Fitness exercise not found!", ErrorCode.FITNESS_EXERCISE_ERROR_NOT_FOUND);
        }
        apiMessageDto.setData(fitnessExerciseMapper.fromEntityToDto(fitnessExercise));
        apiMessageDto.setMessage("Get fitness exercise success");
        return apiMessageDto;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EXERCISE_L')")
    public ApiMessageDto<ResponseListDto<FitnessExerciseDto>> list(FitnessExerciseCriteria fitnessExerciseCriteria, Pageable pageable) {
        ApiMessageDto<ResponseListDto<FitnessExerciseDto>> apiMessageDto = new ApiMessageDto<>();
        fitnessExerciseCriteria.setStatus(MgrConstant.STATUS_ACTIVE);
        Page<FitnessExercise> page = fitnessExerciseRepository.findAll(fitnessExerciseCriteria.getSpecification(), pageable);
        List<FitnessExerciseDto> fitnessExerciseDtos = fitnessExerciseMapper.fromEntityToDtoList(page.getContent());

        ResponseListDto<FitnessExerciseDto> responseListDto = new ResponseListDto(fitnessExerciseDtos, page.getTotalElements(), page.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("List fitness exercise success");
        return apiMessageDto;
    }

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EXERCISE_C')")
    public ApiMessageDto<String> createFitnessExercise(@Valid @RequestBody CreateFitnessExerciseForm createFitnessExerciseForm, BindingResult bindingResult) {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        FitnessExercise fitnessExercise = fitnessExerciseMapper.fromCreateFormToEntity(createFitnessExerciseForm);
        fitnessExerciseRepository.save(fitnessExercise);
        apiMessageDto.setMessage("Create a new fitness exercise success");
        return apiMessageDto;
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EXERCISE_U')")
    public ApiMessageDto<String> updateFitnessExercise(@Valid @RequestBody UpdateFitnessExerciseForm updateFitnessExerciseForm, BindingResult bindingResult) {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        FitnessExercise fitnessExercise = fitnessExerciseRepository.findById(updateFitnessExerciseForm.getId()).orElse(null);
        if (fitnessExercise == null) {
            throw new NotFoundException("Fitness exercise not found!", ErrorCode.FITNESS_EXERCISE_ERROR_NOT_FOUND);
        }
        fitnessExercise.setName(updateFitnessExerciseForm.getName());
        fitnessExercise.setTargetMuscleGroup(updateFitnessExerciseForm.getTargetMuscleGroup());
        fitnessExercise.setDifficultyLevel(updateFitnessExerciseForm.getDifficultyLevel());
        fitnessExercise.setInstructions(updateFitnessExerciseForm.getInstructions());
        fitnessExerciseRepository.save(fitnessExercise);
        apiMessageDto.setMessage("Update fitness exercise success.");
        return apiMessageDto;
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EXERCISE_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        FitnessExercise fitnessExercise = fitnessExerciseRepository.findById(id).orElse(null);
        if (fitnessExercise == null) {
            throw new NotFoundException("Fitness exercise not found!", ErrorCode.FITNESS_EXERCISE_ERROR_NOT_FOUND);
        }
        fitnessExerciseRepository.deleteById(id);
        return makeSuccessResponse("Delete fitness exercise success");
    }
}
