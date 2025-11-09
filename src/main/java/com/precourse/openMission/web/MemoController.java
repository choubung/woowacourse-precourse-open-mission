package com.precourse.openMission.web;

import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/home/memos")
@RestController
public class MemoController {
    private final MemoService memoService; // 의존성 주입

    // TODO: @PostMapping

    // 전체 게시글 목록 조회
    @GetMapping
    public List<MemoListResponseDto> findAllPublicMemo() {
        return memoService.findAllPublicDesc();
    }

    // 특정 게시글 조회
    @GetMapping("/{memo_id}")
    public MemoResponseDto getMemo(@PathVariable Long memo_id) {
        return memoService.findById(memo_id);
    }

    // TODO: @PutMapping
    // TODO: @DeleteMapping
}
