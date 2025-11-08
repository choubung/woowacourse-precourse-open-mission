package com.precourse.openMission.Sevice;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class MemoServiceTest {
    @Mock
    private MemoRepository memoRepository;

    @InjectMocks
    private MemoService memoService;

    @DisplayName("게시글 아이디로 특정 게시글을 조회한다.")
    @Test
    void 아이디로_게시글_찾기_테스트(){
        // given
        Long targetId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(MemoScope.EVERYONE, expectedContent);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(targetId);

        // when
        final MemoResponseDto memoResponseDto = memoService.findById(targetId);

        // then
        assertThat(memoResponseDto).isNotNull();
        assertThat(memoResponseDto.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("게시글 아이디가 존재하지 않으면 IllegalArgumentException이 발생한다.")
    @Test
    void 아이디가_존재하지_않으면_예외가_발생한다() {
        // given
        Long invalidId = 1L;

        doReturn(Optional.empty()).when(memoRepository).findById(invalidId);

        // when
        assertThatThrownBy(() -> memoService.findById(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 게시글이 없습니다.");
    }

    @DisplayName("전체 공개 게시글을 최신순으로 조회한다.")
    @Test
    void 전체_공개_게시글_조회_테스트(){
        // given
        Memo memo1 = createMemo(MemoScope.EVERYONE, "공개글 1");
        Memo memo2 = createMemo(MemoScope.EVERYONE, "공개글 2");

        List<Memo> mockPublicMemos = List.of(memo1, memo2);
        doReturn(mockPublicMemos).when(memoRepository).findAllPublicDesc();

        // when
        final List<MemoListResponseDto> memoListResponseDtos = memoService.findAllPublicDesc();

        // then
        assertThat(memoListResponseDtos).hasSize(2);
        assertThat(memoListResponseDtos.get(0).getContent()).isEqualTo("공개글 1");
        assertThat(memoListResponseDtos.get(1).getContent()).isEqualTo("공개글 2");
    }

    private Memo createMemo(MemoScope scope, String content) {
        return Memo.builder()
                .user(new User())
                .scope(String.valueOf(scope))
                .content(content)
                .memoDate(LocalDateTime.now())
                .build();
    }
}
