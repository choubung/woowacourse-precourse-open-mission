package com.precourse.openMission.service;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import com.precourse.openMission.web.dto.memo.MemoUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MemoService {
    private final MemoRepository memoRepository; // 의존성 주입
    private final UserRepository userRepository;

    @Transactional
    public Long saveMemo(MemoSaveRequestDto requestDto) { // TODO 로그인한 사용자로 자동 등록 필요
        Memo memo = createMemo(requestDto);
        return memoRepository.save(memo).getId();
    }

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

    @Transactional
    public Long updateMemo(Long id, MemoUpdateRequestDto requestDto) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        memo.update(requestDto.getScope().name(), requestDto.getContent(), requestDto.getMemoDate());

        return id;
    }

    @Transactional
    public void deleteMemo(Long id) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        memoRepository.delete(memo);
    }

    private Memo createMemo(MemoSaveRequestDto requestDto) {
        Optional<User> user = userRepository.findById(requestDto.getUserId());

        return Memo.builder()
                .user(user.orElse(null))
                .content(requestDto.getContent())
                .scope(requestDto.getScope().name())
                .memoDate(requestDto.getMemoDate())
                .build();
    }
}
