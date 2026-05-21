package com.mgr.api.controller;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.ApiResponse;
import com.mgr.api.dto.ErrorCode;
import com.mgr.api.dto.ResponseListDto;
import com.mgr.api.dto.account.AccountDto;
import com.mgr.api.exception.BadRequestException;
import com.mgr.api.exception.NotFoundException;
import com.mgr.api.exception.UnauthorizationException;
import com.mgr.api.form.account.CreateAccountAdminForm;
import com.mgr.api.form.account.UpdateAccountAdminForm;
import com.mgr.api.form.account.UpdateProfileAdminForm;
import com.mgr.api.mapper.AccountMapper;
import com.mgr.api.model.Account;
import com.mgr.api.model.Group;
import com.mgr.api.model.criteria.AccountCriteria;
import com.mgr.api.repository.AccountRepository;
import com.mgr.api.repository.GroupRepository;
import com.mgr.api.service.MgrApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/account")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AccountController extends ABasicController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private MgrApiService mgrApiService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_C')")
    @Transactional
    public ApiResponse<String> createAdmin(@Valid @RequestBody CreateAccountAdminForm createAccountAdminForm, BindingResult bindingResult) {
        ApiResponse<String> apiMessageDto = new ApiResponse<>();
        Account account = accountRepository.findFirstByUsername(createAccountAdminForm.getUsername()).orElse(null);
        if (!isSuperAdmin()) {
            throw new BadRequestException("Can not create admin", ErrorCode.ACCOUNT_ERROR_UNABLE_CREATE);
        }
        if (account != null) {
            throw new BadRequestException("Username is existed!", ErrorCode.ACCOUNT_ERROR_USERNAME_EXISTED);
        }
        Group group = groupRepository.findById(createAccountAdminForm.getGroupId()).orElse(null);
        if (group == null) {
            throw new NotFoundException("Group not found!", ErrorCode.GROUP_ERROR_NOT_FOUND);
        }
        account = new Account();
        account.setUsername(createAccountAdminForm.getUsername());
        account.setPassword(passwordEncoder.encode(createAccountAdminForm.getPassword()));
        account.setFullName(createAccountAdminForm.getFullName());
        account.setKind(MgrConstant.USER_KIND_ADMIN);
        account.setEmail(createAccountAdminForm.getEmail());
        account.setGroup(group);
        account.setStatus(createAccountAdminForm.getStatus());
        account.setPhone(createAccountAdminForm.getPhone());
        if (StringUtils.isNoneBlank(createAccountAdminForm.getAvatarPath())) {
            account.setAvatarPath(createAccountAdminForm.getAvatarPath());
        }
        accountRepository.save(account);

        apiMessageDto.setMessage("Create an account admin success.");
        return apiMessageDto;
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_U')")
    public ApiResponse<String> updateAdmin(@Valid @RequestBody UpdateAccountAdminForm updateAccountAdminForm, BindingResult bindingResult) {
        ApiResponse<String> apiMessageDto = new ApiResponse<>();
        if (!isSuperAdmin()) {
            throw new BadRequestException("Can not update admin", ErrorCode.ACCOUNT_ERROR_UNABLE_UPDATE);
        }
        Account account = accountRepository.findById(updateAccountAdminForm.getId()).orElse(null);
        if (account == null) {
            throw new NotFoundException("Account not found!", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }
        Group group = groupRepository.findById(updateAccountAdminForm.getGroupId()).orElse(null);
        if (group == null) {
            throw new NotFoundException("Group not found!", ErrorCode.GROUP_ERROR_NOT_FOUND);
        }
        if (StringUtils.isNoneBlank(updateAccountAdminForm.getPassword())) {
            account.setPassword(passwordEncoder.encode(updateAccountAdminForm.getPassword()));
        }
        account.setFullName(updateAccountAdminForm.getFullName());
        if (StringUtils.isNoneBlank(updateAccountAdminForm.getAvatarPath())) {
            if (account.getAvatarPath() != null && !updateAccountAdminForm.getAvatarPath().equals(account.getAvatarPath())) {
                //delete old image
                mgrApiService.deleteFile(account.getAvatarPath());
            }
            account.setAvatarPath(updateAccountAdminForm.getAvatarPath());
        }
        account.setGroup(group);
        account.setStatus(updateAccountAdminForm.getStatus());
        account.setEmail(updateAccountAdminForm.getEmail());
        account.setPhone(updateAccountAdminForm.getPhone());
        accountRepository.save(account);

        apiMessageDto.setMessage("Update account admin success.");
        return apiMessageDto;

    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_V')")
    public ApiResponse<Account> getAccount(@PathVariable("id") Long id) {
        ApiResponse<Account> apiMessageDto = new ApiResponse<>();
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new NotFoundException("Account not found!", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }
        apiMessageDto.setData(account);
        apiMessageDto.setMessage("Get account success.");
        return apiMessageDto;
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AccountDto> profile() {
        long id = getCurrentUser();
        Account account = accountRepository.findById(id).orElse(null);
        ApiResponse<AccountDto> apiMessageDto = new ApiResponse<>();
        if (account == null) {
            throw new NotFoundException("Account not found!", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }
        apiMessageDto.setData(accountMapper.fromAccountToDto(account));
        apiMessageDto.setMessage("Get Account success");
        return apiMessageDto;
    }

    @PutMapping(value = "/update-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<String> updateProfile(@Valid @RequestBody UpdateProfileAdminForm updateProfileAdminForm, BindingResult bindingResult) {
        ApiResponse<String> apiMessageDto = new ApiResponse<>();
        long id = getCurrentUser();
        var account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new NotFoundException("Account not found!", ErrorCode.ACCOUNT_ERROR_NOT_FOUND);
        }
        if (!passwordEncoder.matches(updateProfileAdminForm.getOldPassword(), account.getPassword())) {
            throw new BadRequestException("Old password is wrong!", ErrorCode.ACCOUNT_ERROR_WRONG_PASSWORD);
        }

        if (StringUtils.isNoneBlank(updateProfileAdminForm.getPassword())) {
            account.setPassword(passwordEncoder.encode(updateProfileAdminForm.getPassword()));
        }
        account.setPhone(updateProfileAdminForm.getPhone());
        account.setFullName(updateProfileAdminForm.getFullName());
        account.setAvatarPath(updateProfileAdminForm.getAvatarPath());
        accountRepository.save(account);

        apiMessageDto.setMessage("Update admin account success");
        return apiMessageDto;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_L')")
    public ApiResponse<ResponseListDto<AccountDto>> listAccount(AccountCriteria accountCriteria, Pageable pageable) {
        if (!isSuperAdmin()) {
            throw new UnauthorizationException("Not allowed to list account.");
        }
        ApiResponse<ResponseListDto<AccountDto>> apiMessageDto = new ApiResponse<>();
        Page<Account> page = accountRepository.findAll(accountCriteria.getSpecification(), pageable);
        ResponseListDto<AccountDto> responseListDto = new ResponseListDto(accountMapper.fromEntityToAccountDtoList(page.getContent()), page.getTotalElements(), page.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("List account success.");
        return apiMessageDto;
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ResponseListDto<AccountDto>> autoComplete(AccountCriteria accountCriteria, Pageable pageable) {
        accountCriteria.setStatus(MgrConstant.STATUS_ACTIVE);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createdDate")));
        ApiResponse<ResponseListDto<AccountDto>> apiMessageDto = new ApiResponse<>();
        Page<Account> page = accountRepository.findAll(accountCriteria.getSpecification(), pageable);
        ResponseListDto<AccountDto> responseListDto = new ResponseListDto(accountMapper.convertAccountToAutoCompleteDto(page.getContent()), page.getTotalElements(), page.getTotalPages());
        apiMessageDto.setData(responseListDto);
        apiMessageDto.setMessage("List account success.");
        return apiMessageDto;
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ACC_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Account] Account not found!", ErrorCode.ACCOUNT_ERROR_NOT_FOUND));
        if (account.getIsSuperAdmin()) {
            throw new BadRequestException("[Account] Account super admin cannot delete", ErrorCode.ACCOUNT_ERROR_UNABLE_DELETE);
        }
        // delete avatar file
        String avatarPath = account.getAvatarPath();
        if (StringUtils.isNoneBlank(avatarPath)) {
            mgrApiService.deleteFile(account.getAvatarPath());
        }
        accountRepository.deleteById(id);
        return makeSuccessResponse("Delete Account success");
    }
}
