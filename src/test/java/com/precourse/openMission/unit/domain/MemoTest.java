package com.precourse.openMission.unit.domain;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class MemoTest {
    private Memo memo;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        memo = Memo.builder()
                .user(user)
                .content("test")
                .scope(MemoScope.SECRET.name())
                .memoDate(LocalDateTime.of(2025, Month.NOVEMBER, 7, 15, 30))
                .build();
    }

    @DisplayName("본문, 공개범위, 날짜를 업데이트하는 테스트")
    @Test
    void update_테스트(){
        // given
        String updatedContent = "updated test";
        MemoScope updatedScope = MemoScope.PUBLIC;
        LocalDateTime updatedDate = LocalDateTime.of(2025, Month.NOVEMBER, 10, 15, 30);

        // when
        memo.update(updatedScope.name(),updatedContent, updatedDate);

        // then
        assertThat(memo.getContent()).isEqualTo(updatedContent);
        assertThat(memo.getScope()).isEqualTo(updatedScope);
        assertThat(memo.getMemoDate()).isEqualTo(updatedDate);
    }
}
