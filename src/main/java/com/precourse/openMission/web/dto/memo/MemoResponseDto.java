package com.precourse.openMission.web.dto.memo;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;

import java.time.LocalDateTime;

public class MemoResponseDto {
    private Long memoId;
    private User user;
    private MemoScope scope;
    private String content;
    private LocalDateTime memoDate;

    public MemoResponseDto(Memo memo) {
        this.memoId = memo.getId();
        this.user = memo.getUser();
        this.scope = memo.getScope();
        this.content = memo.getContent();
        this.memoDate = memo.getMemoDate();
    }
}
