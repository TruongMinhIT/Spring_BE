package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.user.UserDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.user.CreateUserForm;
import com.mgr.api.form.user.UpdateUserForm;
import com.mgr.api.mapper.UserMapper;
import com.mgr.api.model.Account;
import com.mgr.api.model.User;
import com.mgr.api.repository.AccountRepository;
import com.mgr.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class UserController extends ABasicController{
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper userMapper;

    @Autowired
    AccountRepository accountRepository;

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_V')")
    public ApiMessageDto<UserDto> getProfile(){
        ApiMessageDto<UserDto> apiMessageDto = new ApiMessageDto<>();
        long accountId = getCurrentUser();
        User user = userRepository.findById(accountId).orElse(null);
        if(user == null){
            throw new NotFoundException("Account did not have user profile!", ErrorCode.USER_ERROR_NOT_FOUND);
        }
        apiMessageDto.setData(userMapper.fromUserToDto(user));
        apiMessageDto.setMessage("Get user success");
        return apiMessageDto;
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_C')")
    public ApiMessageDto<String> createProfile(@Valid @RequestBody CreateUserForm createUserForm, BindingResult bindingResult){
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        long accountId = getCurrentUser();

        Account account = accountRepository.findById(accountId).orElse(null);
        if(account == null){
            throw new BadRequestException("Account not found", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }

        User existUser = userRepository.findById(accountId).orElse(null);
        if(existUser != null){
            throw new BadRequestException("User already exist for this account");
        }
        User user = userMapper.fromCreateUserFormToEntity(createUserForm);
        user.setAccount(account);
        userRepository.save(user);
        apiMessageDto.setMessage("Create User success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_U')")
    public ApiMessageDto<String> updateProfile(@Valid @RequestBody UpdateUserForm updateUserForm, BindingResult bindingResult){
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        long accountId = getCurrentUser();

        User user = userRepository.findById(accountId).orElse(null);
        if (user == null){
            throw new NotFoundException("User for this profile not found. Please create first!", ErrorCode.USER_ERROR_NOT_FOUND);
        }
        if (updateUserForm.getGender() != null){
            user.setGender(updateUserForm.getGender());
        }
        if (updateUserForm.getDateOfBirth() != null){
            user.setDateOfBirth(updateUserForm.getDateOfBirth());
        }
        userRepository.save(user);
        apiMessageDto.setMessage("Update profile success");
        return apiMessageDto;
    }

    @Transactional
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_D')")
    public ApiMessageDto<String> deleteMyProfile() {
        long accountId = getCurrentUser();

        User user = userRepository.findById(accountId).orElse(null);
        if (user == null) {
            throw new NotFoundException("User profile not found!");
        }

        userRepository.delete(user);
        return makeSuccessResponse("Delete user profile success.");
    }}
