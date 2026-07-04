package com.mgr.api.controller;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.constant.UserExportStatus;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.user.UserDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.exception.UnauthorizationException;
import com.mgr.api.jwt.MgrJwt;
import com.mgr.api.service.UserExportService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.mgr.api.form.user.CreateUserForm;
import com.mgr.api.form.user.UpdateUserForm;
import com.mgr.api.form.user.UpdateUserProfileForm;
import com.mgr.api.mapper.AccountMapper;
import com.mgr.api.mapper.UserMapper;
import com.mgr.api.model.Account;
import com.mgr.api.model.Group;
import com.mgr.api.model.News;
import com.mgr.api.model.User;
import com.mgr.api.model.criteria.UserCriteria;
import com.mgr.api.repository.master.AccountRepository;
import com.mgr.api.repository.master.GroupRepository;
import com.mgr.api.repository.tenant.NewsRepository;
import com.mgr.api.repository.master.UserRepository;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private UserExportService userExportService;

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
    public ApiMessageDto<String> updateUser(@Valid @RequestBody UpdateUserForm updateUserForm, BindingResult bindingResult){
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
        List<String> filesToDelete = new ArrayList<>();
        String avatarPath = user.getAccount().getAvatarPath();
        if (StringUtils.isNoneBlank(avatarPath)) {
            filesToDelete.add(avatarPath);
        }
        List<News> newsList = newsRepository.findAllByUserId(id);
        if (!newsList.isEmpty()) {
            filesToDelete.addAll(newsList.stream()
                    .map(News::getThumbnailUrl)
                    .filter(StringUtils::isNoneBlank)
                    .collect(Collectors.toList()));
            newsRepository.deleteInBatch(newsList);
        }
        if (!filesToDelete.isEmpty()) {
            mgrApiService.deleteFiles(filesToDelete);
        }
        userRepository.delete(user);
        return makeSuccessResponse("Delete user profile success.");
    }

    @ApiOperation(value = "Export tenant users as CSV",
            notes = "ADMIN only. Streams a text/csv download of the current tenant's users "
                    + "(UTF-8 with BOM, Vietnamese headers). Optional status filter: active|locked.")
    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize("isAuthenticated()")
    public void exportUsers(
            @ApiParam(value = "Filter by account status", allowableValues = "active,locked")
            @RequestParam(name = "status", required = false) String status,
            HttpServletResponse response) throws IOException {
        // Authorize before writing any byte so failures render through the global @ControllerAdvice
        // in the standard ApiMessageDto envelope (FR-009, FR-010).
        MgrJwt session = getSessionFromToken();
        boolean isAdmin = session != null
                && (Boolean.TRUE.equals(session.getIsSuperAdmin())
                    || MgrConstant.USER_KIND_ADMIN.equals(session.getUserKind()));
        if (!isAdmin) {
            throw new UnauthorizationException("Only administrators can export users");
        }

        // Resolve and validate the optional status filter before writing (FR-011).
        UserExportStatus statusFilter;
        try {
            statusFilter = UserExportStatus.fromRequestValue(status).orElse(null);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), ErrorCode.USER_ERROR_INVALID_EXPORT_STATUS);
        }

        String fileName = "users-export-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        userExportService.writeUsersCsv(statusFilter, response.getOutputStream());
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USE_P')")
    public ApiMessageDto<UserDto> profile() {
        Long currentUserId = getCurrentUser();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));
        return makeSuccessResponse(userMapper.fromUserToDto(user), "Get profile scuccess");
    }

    @PutMapping(value = "/update-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<String> updateProfile(@Valid @RequestBody UpdateUserProfileForm updateUserProfileForm, BindingResult bindingResult) {
        Long currentUserId = getCurrentUser();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_ERROR_NOT_FOUND));
        Account userAccount = user.getAccount();
        if (StringUtils.isNoneBlank(updateUserProfileForm.getUsername())) {
            if (accountRepository.existsByUsernameAndIdNot(updateUserProfileForm.getUsername(), currentUserId)) {
                throw new BadRequestException("Username is existed", ErrorCode.ACCOUNT_ERROR_USERNAME_EXISTED);
            }
            userAccount.setUsername(updateUserProfileForm.getUsername());
        }
        if (StringUtils.isNoneBlank(updateUserProfileForm.getEmail())) {
            if (accountRepository.existsByEmailAndIdNot(updateUserProfileForm.getEmail(), currentUserId)) {
                throw new BadRequestException("Email is existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
            }
            userAccount.setEmail(updateUserProfileForm.getEmail());
        }
        if (StringUtils.isNoneBlank(updateUserProfileForm.getPhone())) {
            if (accountRepository.existsByPhoneAndIdNot(updateUserProfileForm.getPhone(), currentUserId)) {
                throw new BadRequestException("Phone is existed", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
            }
            userAccount.setPhone(updateUserProfileForm.getPhone());
        }
        if (StringUtils.isNoneBlank(updateUserProfileForm.getPassword())) {
            userAccount.setPassword(passwordEncoder.encode(updateUserProfileForm.getPassword()));
        }
        if (StringUtils.isNoneBlank(updateUserProfileForm.getFullName())) {
            userAccount.setFullName(updateUserProfileForm.getFullName());
        }
        if (StringUtils.isNoneBlank(updateUserProfileForm.getAvatarPath())) {
            if (userAccount.getAvatarPath() != null && !userAccount.getAvatarPath().equals(updateUserProfileForm.getAvatarPath())) {
                mgrApiService.deleteFile(userAccount.getAvatarPath());
            }
            userAccount.setAvatarPath(updateUserProfileForm.getAvatarPath());
        }
        userRepository.save(user);
        return makeSuccessResponse("Update profile success");
    }
}
