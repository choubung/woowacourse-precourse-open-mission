package com.precourse.openMission.web;

import com.precourse.openMission.config.auth.LoginUser;
import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import com.precourse.openMission.web.dto.memo.MemoUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/home/memos")
@RestController
public class MemoController {
    private final MemoService memoService; // 의존성 주입

    @PostMapping
    public Long saveMemo(@RequestBody MemoSaveRequestDto requestDto, @LoginUser SessionUser user) {
        return memoService.saveMemo(requestDto, user);
    }

    // 전체 게시글 목록 조회
    @GetMapping
    public List<MemoListResponseDto> findAllMemo(@LoginUser SessionUser user) {
        return memoService.findAllDesc(user);
    }

    // 특정 게시글 조회
    @GetMapping("/{memoId}")
    public MemoResponseDto getMemo(@PathVariable Long memoId, @LoginUser SessionUser user) {
        return memoService.findById(memoId, user);
    }

    @PutMapping("/{memoId}")
    public Long updateMemo(@PathVariable Long memoId, @RequestBody MemoUpdateRequestDto requestDto, @LoginUser SessionUser user) {
        return memoService.updateMemo(memoId, requestDto, user);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/{memoId}")
    public void deleteMemo(@PathVariable Long memoId, @LoginUser SessionUser user) {
        memoService.deleteMemo(memoId, user);
    }
}
