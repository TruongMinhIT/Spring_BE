package com.mgr.api.controller;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.user.UserDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.form.user.CreateUserForm;
import com.mgr.api.form.user.UpdateUserForm;
import com.mgr.api.mapper.AccountMapper;
import com.mgr.api.mapper.UserMapper;
import com.mgr.api.model.Account;
import com.mgr.api.model.Group;
import com.mgr.api.model.User;
import com.mgr.api.model.criteria.UserCriteria;
import com.mgr.api.repository.AccountRepository;
import com.mgr.api.repository.GroupRepository;
import com.mgr.api.repository.UserRepository;
import com.mgr.api.service.MgrApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MgrApiService mgrApiService;

    @Autowired
    private AccountMapper accountMapper;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_V')")
    public ApiMessageDto<UserDto> getUser(@PathVariable("id") Long id){
        User user = userRepository.findById(id).orElse(null);
        if (user == null){
            throw new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND);
        }
        return makeSuccessResponse(userMapper.fromUserToDto(user), "Get user succes");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_C')")
    public ApiMessageDto<String> createUser(@Valid @RequestBody CreateUserForm createUserForm, BindingResult bindingResult){
        if(accountRepository.findFirstByUsername(createUserForm.getUsername()).isPresent()){
            throw new BadRequestException("Username is existed", ErrorCode.ACCOUNT_ERROR_USERNAME_EXISTED);
        }
        if(accountRepository.findFirstByEmail(createUserForm.getEmail()).isPresent()){
            throw new BadRequestException("Email is existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
        }
        if(accountRepository.findFirstByPhone(createUserForm.getPhone()).isPresent()){
            throw new BadRequestException("Phone is existed", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
        }
        Group group = groupRepository.findById(createUserForm.getGroupId())
                .orElseThrow(()->new NotFoundException("Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));
        if(!group.getKind().equals(MgrConstant.GROUP_KIND_USER)){
            throw new BadRequestException("Group not for user", ErrorCode.USER_ERROR_UNABLE_CREATE);
        }

        Account account = accountMapper.fromCreateUserFormToAccount(createUserForm);
        account.setPassword(passwordEncoder.encode(createUserForm.getPassword()));
        account.setKind(MgrConstant.USER_KIND_USER);
        account.setGroup(group);

        User user = userMapper.fromCreateUserFormToEntity(createUserForm);
        user.setAccount(account);
        userRepository.save(user);
        return makeSuccessResponse("Create User success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_U')")
    public ApiMessageDto<String> updateProfile(@Valid @RequestBody UpdateUserForm updateUserForm, BindingResult bindingResult){
        User user = userRepository.findById(updateUserForm.getId())
                .orElseThrow(()-> new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));

        Account account = user.getAccount();
        if (updateUserForm.getGroupId()!=null){
            if (!isSuperAdmin()){
                throw new BadRequestException("Can not update Group without superadmin", ErrorCode.USER_ERROR_UNABLE_UPDATE);
            }
            Group group = groupRepository.findById(updateUserForm.getGroupId())
                    .orElseThrow(()->new NotFoundException("Group not found!", ErrorCode.GROUP_ERROR_NOT_FOUND));
            account.setGroup(group);
        }
        if (StringUtils.isNoneBlank(updateUserForm.getEmail()) && !updateUserForm.getEmail().equals(account.getEmail())){
            if (accountRepository.existsByEmailAndIdNot(updateUserForm.getEmail(), updateUserForm.getId())){
                throw new BadRequestException("Email already taken", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
            }
            account.setEmail(updateUserForm.getEmail());
        }
        if (StringUtils.isNoneBlank(updateUserForm.getPhone()) && !updateUserForm.getPhone().equals(account.getPhone())){
            if (accountRepository.existsByPhoneAndIdNot(updateUserForm.getPhone(), updateUserForm.getId())){
                throw new BadRequestException("Phone already taken", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
            }
            account.setPhone(updateUserForm.getPhone());
        }

        if (StringUtils.isNoneBlank(updateUserForm.getPassword())){
            account.setPassword(passwordEncoder.encode(updateUserForm.getPassword()));
        }
        if (StringUtils.isNoneBlank(updateUserForm.getFullName())){
            account.setFullName(updateUserForm.getFullName());
        }
        if (updateUserForm.getStatus() != null) {
            account.setStatus(updateUserForm.getStatus());
        }
        if (StringUtils.isNoneBlank(updateUserForm.getAvatarPath())){
            if (account.getAvatarPath()!=null && !updateUserForm.getAvatarPath().equals(account.getAvatarPath())){
                //delete old image
                mgrApiService.deleteFile(account.getAvatarPath());
            }
            account.setAvatarPath(updateUserForm.getAvatarPath());
        }
        if (updateUserForm.getGender()!=null){
            user.setGender(updateUserForm.getGender());
        }
        if (updateUserForm.getDateOfBirth()!=null){
            user.setDateOfBirth(updateUserForm.getDateOfBirth());
        }
        userRepository.save(user);
        return makeSuccessResponse("Update user success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_L')")
    public ApiMessageDto<ResponseListDto<UserDto>> listUser(UserCriteria userCriteria, Pageable pageable){
        Page<User> page = userRepository.findAll(userCriteria.getSpecification(), pageable);
        ResponseListDto<UserDto> responseListDto = new ResponseListDto(userMapper.fromEntityToUserDtoList(page.getContent()),
                                                                                    page.getTotalElements(), page.getTotalPages());
        return makeSuccessResponse(responseListDto,"List account success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));
        String avatarPath = user.getAccount().getAvatarPath();
        if(StringUtils.isNoneBlank(avatarPath)){
            mgrApiService.deleteFile(avatarPath);
        }
        userRepository.delete(user);
        return makeSuccessResponse("Delete user profile success.");
    }}
