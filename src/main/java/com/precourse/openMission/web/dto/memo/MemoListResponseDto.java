package com.precourse.openMission.web.dto.memo;

import com.precourse.openMission.domain.memo.Memo;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemoListResponseDto {
    private Long memoId;
    private Long userId;
    private String userName;
    private String content;
    private LocalDateTime memoDate;

    public MemoListResponseDto(Memo memo) {
        this.memoId = memo.getId();
        this.userId = memo.getUser().getId();
        this.userName = memo.getUser().getName();
        this.content = memo.getContent();
        this.memoDate = memo.getMemoDate();
    }
}
