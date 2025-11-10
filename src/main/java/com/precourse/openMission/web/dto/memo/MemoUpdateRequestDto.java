package com.precourse.openMission.web.dto.memo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MemoUpdateRequestDto {
    private String content;
    private MemoScope scope;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime memoDate;

    @Builder
    public MemoUpdateRequestDto(String content, MemoScope scope, LocalDateTime memoDate) {
        this.content = content;
        this.scope = scope;
        this.memoDate = memoDate;
    }
}
