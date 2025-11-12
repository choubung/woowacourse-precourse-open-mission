package com.precourse.openMission.web;

import com.precourse.openMission.config.auth.LoginUser;
import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/home/users")
@RestController
public class UserApiController {
    private final UserService userService;
    private final HttpSession session;

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/me")
    public void deleteUser(@LoginUser SessionUser user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        try {
            userService.deleteUser(user);
        } finally {
            session.invalidate();
        }
    }
}
