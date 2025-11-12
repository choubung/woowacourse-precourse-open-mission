package com.precourse.openMission.unit.web;

import com.precourse.openMission.config.auth.LoginUserArgumentResolver;
import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.domain.user.Role;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.exception.GlobalExceptionHandler;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.service.UserService;
import com.precourse.openMission.web.UserApiController;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserApiControllerTest {
    private User user;

    @Mock
    private UserService userService;

    @Mock
    private HttpSession mockSession;

    @InjectMocks
    private UserApiController userApiController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        LoginUserArgumentResolver loginUserArgumentResolver =
                new LoginUserArgumentResolver(mockSession);

        mockMvc = MockMvcBuilders.standaloneSetup(userApiController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(loginUserArgumentResolver)
                .build();

        user = User.builder()
                .name("일반 사용자")
                .role(Role.USER)
                .email("user@test.com")
                .build();
    }

    @DisplayName("회원 탈퇴시 회원 정보가 삭제된다.")
    @Test
    void 회원_탈퇴_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/users/me")
        );

        // then
        resultActions.andExpect(status().isNoContent());
        verify(userService).deleteUser(sessionUser);
    }

    @DisplayName("비로그인 사용자가 탈퇴 시도시 400 Bad Request가 발생한다.")
    @Test
    void 비로그인_사용자_탈퇴_시도시_예외가_발생한다() throws Exception {
        // given
        when(mockSession.getAttribute("user")).thenReturn(null);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/users/me")
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }
}
