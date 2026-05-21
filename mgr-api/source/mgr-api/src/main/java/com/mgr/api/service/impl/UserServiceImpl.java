package com.mgr.api.service.impl;

import com.mgr.api.constant.MgrConstant;
import com.mgr.api.jwt.MgrJwt;
import com.mgr.api.model.Account;
import com.mgr.api.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service(value = "userService")
@Slf4j
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userId) {
        Account user = accountRepository.findFirstByUsername(userId).orElse(null);
        if (user == null) {
            log.error("Invalid username or password!");
            throw new UsernameNotFoundException("Invalid username or password!");
        }
        boolean enabled = true;
        if (user.getStatus() != MgrConstant.STATUS_ACTIVE) {
            log.error("User had been locked!");
            enabled = false;
        }
        Set<GrantedAuthority> grantedAuthorities = getAccountPermission(user);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), enabled, true, true, true, grantedAuthorities);
    }

    public OAuth2AccessToken getAccessTokenForCustom(ClientDetails client,
                                                     TokenRequest tokenRequest,
                                                     String username,
                                                     String password,
                                                     String tenant,
                                                     String grantType,
                                                     AuthorizationServerTokenServices tokenServices) throws GeneralSecurityException, IOException {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("grantType", grantType);
        requestParameters.put("tenantId", tenant);
        String clientId = client.getClientId();
        boolean approved = true;
        Set<String> responseTypes = new HashSet<>();
        responseTypes.add("code");
        Map<String, Serializable> extensionProperties = new HashMap<>();

        Account account = accountRepository.findFirstByUsername(username).orElse(null);
        if (account == null) {
            log.error("Invalid username or password.");
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        if (!passwordEncoder.matches(password, account.getPassword())) {
            log.error("Invalid username or password.");
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        boolean enabled = true;
        if (account.getStatus() != MgrConstant.STATUS_ACTIVE) {
            log.error("User had been locked");
            enabled = false;
        }

        Set<GrantedAuthority> grantedAuthorities = getAccountPermission(account);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(account.getUsername(), account.getPassword(), enabled, true, true, true, grantedAuthorities);

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId,
                userDetails.getAuthorities(), approved, client.getScope(),
                client.getResourceIds(), null, responseTypes, extensionProperties);
        org.springframework.security.core.userdetails.User userPrincipal = new org.springframework.security.core.userdetails.User(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), userDetails.isAccountNonExpired(), userDetails.isCredentialsNonExpired(), userDetails.isAccountNonLocked(), userDetails.getAuthorities());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userDetails.getAuthorities());
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);
        return tokenServices.createAccessToken(auth);
    }

    private Set<GrantedAuthority> getAccountPermission(Account user) {
        List<String> roles = new ArrayList<>();
        user.getGroup().getPermissions().stream().filter(f -> f.getPermissionCode() != null).forEach(pName -> roles.add(pName.getPermissionCode()));
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())).collect(Collectors.toSet());
    }

    public MgrJwt getAddInfoFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                Map<String, Object> map = (Map<String, Object>) oauthDetails.getDecodedDetails();
                String encodedData = (String) map.get("additional_info");
                //idStr -> json
                if (encodedData != null && !encodedData.isEmpty()) {
                    return MgrJwt.decode(encodedData);
                }
                return null;
            }
        }
        return null;
    }
}
