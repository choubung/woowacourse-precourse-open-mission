package com.precourse.openMission.service;

import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.Role;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import com.precourse.openMission.web.dto.memo.MemoUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MemoService {
    private final MemoRepository memoRepository; // 의존성 주입
    private final UserRepository userRepository;

    @Transactional
    public Long saveMemo(MemoSaveRequestDto requestDto, SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new IllegalArgumentException("로그인해야 합니다.");
        }

        Memo memo = createMemo(requestDto, sessionUser);

        return memoRepository.save(memo).getId();
    }

    // 특정 게시글 조회
    public MemoResponseDto findById(Long id, SessionUser sessionUser) {
        Memo memo = memoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        if (memo.getScope().equals(MemoScope.SECRET)) {
            if (sessionUser == null) {
                throw new IllegalArgumentException("글을 보려면 로그인해야 합니다.");
            }

            User user = userRepository.findByEmail(sessionUser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

            if (!user.equals(memo.getUser()) && user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
            }
        }

        return new MemoResponseDto(memo);
    }

    // 모든 전체 공개 게시글 조회
    @Transactional(readOnly = true)
    public List<MemoListResponseDto> findAllDesc(SessionUser sessionUser) {
        if (sessionUser != null) {
            User user = userRepository.findByEmail(sessionUser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

            if (user.getRole() == Role.ADMIN) {
                return memoRepository.findAllDesc().stream()
                        .map(MemoListResponseDto::new)
                        .collect(Collectors.toList());
            }

            return memoRepository.findAllPublicAndMySecretDesc(user).stream()
                    .map(MemoListResponseDto::new)
                    .collect(Collectors.toList());
        }

        return memoRepository.findAllPublicDesc().stream()
                .map(MemoListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long updateMemo(Long id, MemoUpdateRequestDto requestDto, SessionUser sessionUser) {
        Memo memo = validateMemoIdAndUserId(id, sessionUser);

        memo.update(requestDto.getScope().name(), requestDto.getContent(), requestDto.getMemoDate());
        return id;
    }

    @Transactional
    public void deleteMemo(Long id, SessionUser sessionUser) {
        Memo memo = validateMemoIdAndUserId(id, sessionUser);

        memoRepository.delete(memo);
    }

    private Memo createMemo(MemoSaveRequestDto requestDto, SessionUser sessionUser) {
        User user = userRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        return Memo.builder()
                .user(user)
                .content(requestDto.getContent())
                .scope(requestDto.getScope().name())
                .memoDate(requestDto.getMemoDate())
                .build();
    }

    private Memo validateMemoIdAndUserId(Long memoId, SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Memo memo = memoRepository.findById(memoId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        User user = userRepository.findByEmail(sessionUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        if (!memo.getUser().equals(user) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("유효한 사용자가 아닙니다.");
        }

        return memo;
    }
}
