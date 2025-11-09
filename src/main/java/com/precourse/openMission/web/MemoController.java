package com.precourse.openMission.web;

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
    public Long saveMemo(@RequestBody MemoSaveRequestDto requestDto) {
        return memoService.saveMemo(requestDto);
    }

    // 전체 게시글 목록 조회
    @GetMapping
    public List<MemoListResponseDto> findAllPublicMemo() {
        return memoService.findAllPublicDesc();
    }

    // 특정 게시글 조회
    @GetMapping("/{memoId}")
    public MemoResponseDto getMemo(@PathVariable Long memoId) {
        return memoService.findById(memoId);
    }

    @PutMapping("/{memoId}")
    public Long updateMemo(@PathVariable Long memoId, @RequestBody MemoUpdateRequestDto requestDto) {
        return memoService.updateMemo(memoId, requestDto);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/{memoId}")
    public void deleteMemo(@PathVariable Long memoId) {
        memoService.deleteMemo(memoId);
    }
}
