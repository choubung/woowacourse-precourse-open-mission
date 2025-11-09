package com.precourse.openMission.service;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MemoService {
    private final MemoRepository memoRepository; // 의존성 주입

    // TODO: 저장

    // TODO: 업데이트

    // 특정 게시글 조회
    public MemoResponseDto findById(Long id) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        return new MemoResponseDto(memo);
    }

    // 모든 전체 공개 게시글 조회
    @Transactional(readOnly = true)
    public List<MemoListResponseDto> findAllPublicDesc() {
        return memoRepository.findAllPublicDesc().stream()
                .map(MemoListResponseDto::new)
                .collect(Collectors.toList());
    }

    // TODO: 삭제
}
