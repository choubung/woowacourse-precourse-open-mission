package com.precourse.openMission.web.dto.memo;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemoResponseDto {
    private Long memoId;
    private Long userId;
    private String userName;
    private MemoScope scope;
    private String content;
    private LocalDateTime memoDate;

    public MemoResponseDto(Memo memo) {
        this.memoId = memo.getId();
        this.userId = memo.getUser().getId();
        this.userName = memo.getUser().getName();
        this.scope = memo.getScope();
        this.content = memo.getContent();
        this.memoDate = memo.getMemoDate();
    }
}
