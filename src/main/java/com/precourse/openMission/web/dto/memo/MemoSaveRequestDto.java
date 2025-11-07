package com.precourse.openMission.web.dto.memo;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MemoSaveRequestDto {
    private String content;
    private User user;
    private MemoScope scope;
    private LocalDateTime memoDate;

    @Builder
    public MemoSaveRequestDto(String content, User user, MemoScope scope, LocalDateTime memoDate) {
        this.content = content;
        this.user = user; // TODO: 로그인 한 사용자 정보 자동으로 가져오기 구현
        this.scope = scope;
        this.memoDate = memoDate;
    }

    public Memo toEntity() {
        return Memo.builder()
                .content(content)
                .user(user)
                .scope(String.valueOf(scope))
                .memoDate(memoDate)
                .build();
    }
}
