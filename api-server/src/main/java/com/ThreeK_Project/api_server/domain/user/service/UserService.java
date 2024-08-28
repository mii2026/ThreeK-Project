package com.ThreeK_Project.api_server.domain.user.service;

import static com.ThreeK_Project.api_server.domain.user.message.UserExceptionMessage.DUPLICATE_NAME;
import static com.ThreeK_Project.api_server.domain.user.message.UserExceptionMessage.USER_NOT_FOUND;
import static com.ThreeK_Project.api_server.domain.user.message.UserExceptionMessage.USER_REGISTRATION_RESTRICTION;
import static com.ThreeK_Project.api_server.domain.user.message.UserSuccessMessage.DELETE_USER_SUCCESS;
import static com.ThreeK_Project.api_server.domain.user.message.UserSuccessMessage.SIGN_UP_SUCCESS;
import static com.ThreeK_Project.api_server.domain.user.message.UserSuccessMessage.UPDATE_USER_INFO_SUCCESS;

import com.ThreeK_Project.api_server.domain.user.dto.SignUpRequest;
import com.ThreeK_Project.api_server.domain.user.dto.UpdateUserInfoRequest;
import com.ThreeK_Project.api_server.domain.user.dto.UserInfoResponse;
import com.ThreeK_Project.api_server.domain.user.enums.Role;
import com.ThreeK_Project.api_server.domain.user.entity.User;
import com.ThreeK_Project.api_server.domain.user.repository.UserRepository;
import com.ThreeK_Project.api_server.global.dto.SuccessResponse;
import com.ThreeK_Project.api_server.global.exception.ApplicationException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입
    @Transactional
    public SuccessResponse signUp(SignUpRequest requestParam) {
        validateDuplicateName(requestParam.username());
        Role role = validateRole(requestParam.role());
        String encodedPassword = passwordEncoder.encode(requestParam.password());

        saveUser(requestParam, encodedPassword, role);
        return new SuccessResponse(SIGN_UP_SUCCESS.getValue());
    }

    // 이름 중복 검증
    private void validateDuplicateName(String username){
        Optional<User> findMember = userRepository.findByUsername(username);
        if (findMember.isPresent()) {
            throw new ApplicationException(DUPLICATE_NAME.getValue());
        }
    }

    // 역할 검증
    private Role validateRole(String roleValue) {
        Role role = Role.fromValue(roleValue);
        if (role == Role.MANAGER || role == Role.MASTER) {
            throw new ApplicationException(USER_REGISTRATION_RESTRICTION.getValue());
        }
        return role;
    }

    private void saveUser(SignUpRequest requestParam, String encodedPassword, Role role) {
        User user = User.createUser(requestParam.username(), encodedPassword,
                Collections.singletonList(role), requestParam.phoneNumber(), requestParam.address());
        userRepository.save(user);
    }

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(User user) {
        return new UserInfoResponse(user);
    }

    // 회원 정보 수정
    @Transactional
    public SuccessResponse updateUserInfo(User user, UpdateUserInfoRequest requestParam) {
        User findUser = findUser(user.getUsername());

        validateDuplicateName(requestParam.username());
        String encodedPassword = passwordEncoder.encode(requestParam.password());

        updateUser(findUser, requestParam, encodedPassword);
        return new SuccessResponse(UPDATE_USER_INFO_SUCCESS.getValue());
    }

    private void updateUser(User user, UpdateUserInfoRequest requestParam, String encodedPassword) {
        List<Role> originalRoles = user.getRoles();
        LocalDateTime originalCreatedAt = user.getCreatedAt();

        userRepository.delete(user);
        User newUser = User.updateUser(requestParam.username(), encodedPassword, originalRoles, requestParam.phoneNumber(),
                requestParam.address(), originalCreatedAt);
        userRepository.save(newUser);
    }

    // 회원 탈퇴
    @Transactional
    public SuccessResponse deleteUser(User user) {
        User findUser = findUser(user.getUsername());

        findUser.deleteUser(findUser);
        return new SuccessResponse(DELETE_USER_SUCCESS.getValue());
    }

    // 회원 조회
    @Transactional(readOnly = true)
    public User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(USER_NOT_FOUND.getValue()));
    }

}
