package com.ThreeK_Project.api_server.domain.auth.service;

import static com.ThreeK_Project.api_server.domain.auth.message.AuthExceptionMessage.PASSWORD_NOT_MATCH;
import static com.ThreeK_Project.api_server.domain.auth.message.AuthExceptionMessage.USER_NOT_ACTIVE;

import com.ThreeK_Project.api_server.domain.auth.dto.LoginRequest;
import com.ThreeK_Project.api_server.domain.auth.dto.LoginResponse;
import com.ThreeK_Project.api_server.domain.user.entity.User;
import com.ThreeK_Project.api_server.global.exception.ApplicationException;
import com.ThreeK_Project.api_server.global.security.auth.UserDetailsCustom;
import com.ThreeK_Project.api_server.global.security.auth.UserDetailsServiceCustom;
import com.ThreeK_Project.api_server.global.security.jwt.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceCustom userDetailsServiceCustom;
    private final TokenManager tokenManager;

    // 회원 로그인
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest requestParam) {
        UserDetailsCustom userDetails = userDetailsServiceCustom.loadUserByUsername(requestParam.username());
        User user = userDetails.getUser();

        checkUserStatus(user);
        validatePassword(requestParam.password(), userDetails.getUser().getPassword());

        String token = tokenManager.generateToken(userDetails);
        return new LoginResponse(token);
    }

    // 회원 탈퇴 여부 확인
    private void checkUserStatus(User user) {
        if (user.getDeletedAt() != null) {
            throw new ApplicationException(USER_NOT_ACTIVE.getValue());
        }
    }

    // 비밀번호 검증
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ApplicationException(PASSWORD_NOT_MATCH.getValue());
        }
    }

}
