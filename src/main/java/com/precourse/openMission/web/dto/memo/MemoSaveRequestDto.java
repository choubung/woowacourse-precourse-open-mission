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
    private Long userId;
    private MemoScope scope;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime memoDate;

    @Builder
    public MemoSaveRequestDto(String content, Long userId, MemoScope scope, LocalDateTime memoDate) {
        this.content = content;
        this.userId = userId; // TODO: 로그인 한 사용자 정보 자동으로 가져오기 구현
        this.scope = scope;
        this.memoDate = memoDate;
    }
}
