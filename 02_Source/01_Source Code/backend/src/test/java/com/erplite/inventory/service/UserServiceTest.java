package com.erplite.inventory.service;

import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.dto.user.*;
import com.erplite.inventory.entity.User;
import com.erplite.inventory.entity.User.UserRole;
import com.erplite.inventory.exception.BusinessException;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    private static final Pageable PAGE = PageRequest.of(0, 10);

    private User buildUser(String id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@ims.com")
                .password("hashed_password")
                .role(UserRole.Viewer)
                .isActive(true)
                .build();
    }

    // ── listUsers ──────────────────────────────────────────────────────────

    @Test
    void listUsers_noFilter_callsFindAll() {
        Page<User> page = new PageImpl<>(List.of(buildUser("u1", "admin")));
        when(userRepository.findAll(PAGE)).thenReturn(page);

        PagedResponse<UserResponse> result = userService.listUsers(null, null, PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findAll(PAGE);
    }

    @Test
    void listUsers_roleFilter_callsFindByRole() {
        when(userRepository.findByRole(UserRole.Admin, PAGE))
                .thenReturn(new PageImpl<>(List.of()));

        userService.listUsers(UserRole.Admin, null, PAGE);

        verify(userRepository).findByRole(UserRole.Admin, PAGE);
    }

    @Test
    void listUsers_isActiveFilter_callsFindByIsActive() {
        when(userRepository.findByIsActive(true, PAGE)).thenReturn(new PageImpl<>(List.of()));

        userService.listUsers(null, true, PAGE);

        verify(userRepository).findByIsActive(true, PAGE);
    }

    @Test
    void listUsers_roleAndIsActiveFilter_callsCombinedQuery() {
        when(userRepository.findByRoleAndIsActive(UserRole.Production, false, PAGE))
                .thenReturn(new PageImpl<>(List.of()));

        userService.listUsers(UserRole.Production, false, PAGE);

        verify(userRepository).findByRoleAndIsActive(UserRole.Production, false, PAGE);
    }

    // ── getUserById ────────────────────────────────────────────────────────

    @Test
    void getUserById_found_returnsUserResponse() {
        User user = buildUser("u1", "admin");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById("u1");

        assertThat(result.getUserId()).isEqualTo("u1");
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo(UserRole.Viewer);
    }

    @Test
    void getUserById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ── getUserByJwtSub ────────────────────────────────────────────────────

    @Test
    void getUserByJwtSub_found_returnsUser() {
        User user = buildUser("sub-123", "admin");
        when(userRepository.findById("sub-123")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByJwtSub("sub-123");

        assertThat(result.getUserId()).isEqualTo("sub-123");
    }

    @Test
    void getUserByJwtSub_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("unknown-sub")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByJwtSub("unknown-sub"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── createUser ─────────────────────────────────────────────────────────

    @Test
    void createUser_success_savesAndReturnsUser() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@ims.com");
        req.setPassword("Password@1");
        req.setRole(UserRole.Production);

        User saved = buildUser("u2", "newuser");
        saved.setRole(UserRole.Production);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@ims.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse result = userService.createUser(req);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getRole()).isEqualTo(UserRole.Production);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateUsername_throwsBusinessException() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("existing_user");
        req.setEmail("new@ims.com");

        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existing_user");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_duplicateEmail_throwsBusinessException() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("newuser");
        req.setEmail("taken@ims.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@ims.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taken@ims.com");

        verify(userRepository, never()).save(any());
    }

    // ── updateUser ─────────────────────────────────────────────────────────

    @Test
    void updateUser_success_updatesFieldsAndSaves() {
        User existing = buildUser("u1", "oldname");
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUsername("newname");
        req.setEmail("newname@ims.com");
        req.setRole(UserRole.InventoryManager);
        req.setIsActive(true);

        when(userRepository.findById("u1")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponse result = userService.updateUser("u1", req);

        assertThat(result.getUsername()).isEqualTo("newname");
        assertThat(result.getRole()).isEqualTo(UserRole.InventoryManager);
    }

    @Test
    void updateUser_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("x", new UserUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── changePassword ─────────────────────────────────────────────────────

    @Test
    void changePassword_asAdmin_skipsCurrentPasswordCheck() {
        User user = buildUser("u1", "someuser");
        user.setPassword("old_hash");
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setCurrentPassword(null);
        req.setNewPassword("NewPass@123");

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword("u1", req, true);

        assertThat(user.getPassword()).isEqualTo("NewPass@123");
    }

    @Test
    void changePassword_asNonAdmin_correctCurrentPassword_succeeds() {
        User user = buildUser("u1", "someuser");
        user.setPassword("correct_password");
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setCurrentPassword("correct_password");
        req.setNewPassword("NewPass@123");

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword("u1", req, false);

        assertThat(user.getPassword()).isEqualTo("NewPass@123");
    }

    @Test
    void changePassword_asNonAdmin_wrongCurrentPassword_throwsBusinessException() {
        User user = buildUser("u1", "someuser");
        user.setPassword("correct_password");
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setCurrentPassword("wrong_password");
        req.setNewPassword("NewPass@123");

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword("u1", req, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.changePassword("x", new PasswordChangeRequest(), true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deactivateUser ─────────────────────────────────────────────────────

    @Test
    void deactivateUser_success_setsIsActiveFalse() {
        User user = buildUser("u2", "targetuser");
        when(userRepository.findById("u2")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.deactivateUser("u2", "u1");

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_selfDeactivation_throwsBusinessException() {
        User user = buildUser("u1", "admin");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.deactivateUser("u1", "u1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot deactivate your own account");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateUser_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser("x", "u1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getUserActivity ────────────────────────────────────────────────────

    @Test
    void getUserActivity_found_returnsEmptyList() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(buildUser("u1", "admin")));

        var result = userService.getUserActivity("u1");

        assertThat(result).isEmpty();
    }

    @Test
    void getUserActivity_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserActivity("x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
