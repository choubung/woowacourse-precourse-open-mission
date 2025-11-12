package com.precourse.openMission.service;

import com.precourse.openMission.config.auth.dto.SessionUser;
import org.springframework.transaction.annotation.Transactional;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void deleteUser(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new IllegalArgumentException("세션 정보가 없습니다.");
        }
        User user = userRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        userRepository.delete(user);
    }
}
