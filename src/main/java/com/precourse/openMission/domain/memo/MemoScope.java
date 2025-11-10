package com.precourse.openMission.domain.memo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemoScope {
    SECRET("나만보기"),
    PUBLIC("전체공개");

    private final String scope;
}
