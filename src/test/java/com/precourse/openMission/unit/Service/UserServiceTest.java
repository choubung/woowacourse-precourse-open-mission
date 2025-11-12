package com.precourse.openMission.unit.Service;

import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.user.Role;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import com.precourse.openMission.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private MemoRepository memoRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void 회원_탈퇴_테스트() {
        // given
        User user = User.builder()
                .name("사용자")
                .email("test@test.com")
                .role(Role.USER)
                .build();

        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        // when
        userService.deleteUser(sessionUser);

        // then
        verify(userRepository).delete(user);
    }

    @DisplayName("비로그인 사용자가 탈퇴 시도시 예외가 발생한다.")
    @Test
    void 비로그인_사용자_탈퇴_시도시_예외가_발생한다() {
        // when, then
        assertThatThrownBy(() -> userService.deleteUser(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
