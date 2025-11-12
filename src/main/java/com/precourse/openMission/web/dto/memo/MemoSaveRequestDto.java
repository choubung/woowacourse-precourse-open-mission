package com.precourse.openMission.web.dto.memo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.precourse.openMission.domain.memo.MemoScope;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MemoSaveRequestDto {
    private String content;
    private MemoScope scope;
    private LocalDateTime memoDate;

    @Builder
    public MemoSaveRequestDto(String content, MemoScope scope, LocalDateTime memoDate) {
        this.content = content;
        this.scope = scope;
        this.memoDate = memoDate;
    }
}
