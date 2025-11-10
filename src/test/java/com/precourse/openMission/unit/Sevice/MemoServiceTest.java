package com.precourse.openMission.unit.Sevice;

import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import com.precourse.openMission.web.dto.memo.MemoUpdateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemoServiceTest {
    static final LocalDateTime dateTime = LocalDateTime.parse("2025-10-09T15:30:00");

    @Mock
    private MemoRepository memoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemoService memoService;

    @Test
    void 메모_저장_테스트(){
        // given
        Long expectedMemoId = 10L;

        String content = "test";
        Long userId = 1L;
        MemoScope scope = MemoScope.PUBLIC;
        MemoSaveRequestDto memoSaveRequestDto = new MemoSaveRequestDto(content, userId, scope, dateTime);

        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        doReturn(Optional.of(user)).when(userRepository).findById(userId);

        Memo memo = createMemo(user, scope, content);
        ReflectionTestUtils.setField(memo, "id", 10L);
        doReturn(memo).when(memoRepository).save(any());

        // when
        final Long savedMemoId = memoService.saveMemo(memoSaveRequestDto);

        // then
        assertThat(savedMemoId).isEqualTo(expectedMemoId);
    }

    @DisplayName("메모 아이디로 특정 메모를 조회한다.")
    @Test
    void 아이디로_메모_찾기_테스트(){
        // given
        Long targetId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(MemoScope.PUBLIC, expectedContent);
        ReflectionTestUtils.setField(mockMemo, "id", 1L);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(targetId);

        // when
        final MemoResponseDto memoResponseDto = memoService.findById(targetId);

        // then
        assertThat(memoResponseDto).isNotNull();
        assertThat(memoResponseDto.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("조회하려는 메모 아이디가 존재하지 않으면 IllegalArgumentException이 발생한다.")
    @Test
    void 조회_아이디가_존재하지_않으면_예외가_발생한다() {
        // given
        Long invalidId = 1L;

        doReturn(Optional.empty()).when(memoRepository).findById(invalidId);

        // when, then
        assertThatThrownBy(() -> memoService.findById(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 게시글이 없습니다.");
    }

    @DisplayName("전체 공개 메모를 최신순으로 조회한다.")
    @Test
    void 전체_공개_게시글_조회_테스트(){
        // given
        Memo memo1 = createMemo(MemoScope.PUBLIC, "공개글 1");
        Memo memo2 = createMemo(MemoScope.PUBLIC, "공개글 2");

        List<Memo> mockPublicMemos = List.of(memo1, memo2);
        doReturn(mockPublicMemos).when(memoRepository).findAllPublicDesc();

        // when
        final List<MemoListResponseDto> memoListResponseDtos = memoService.findAllPublicDesc();

        // then
        assertThat(memoListResponseDtos).hasSize(2);
        assertThat(memoListResponseDtos.get(0).getContent()).isEqualTo("공개글 1");
        assertThat(memoListResponseDtos.get(1).getContent()).isEqualTo("공개글 2");
    }

    @DisplayName("메모 아이디와 MemoUpdateRequestDto(공개범위, 내용, 날짜)를 받아 메모를 업데이트한다.")
    @Test
    void 메모_업데이트_테스트() {
        // given
        Long memoId = 1L;
        String expectedContent = "업데이트 후";

        Memo mockMemo = createMemo(MemoScope.PUBLIC, "업데이트 전");
        ReflectionTestUtils.setField(mockMemo, "id", memoId);
        MemoUpdateRequestDto memoUpdateRequestDto = new MemoUpdateRequestDto(expectedContent, MemoScope.PUBLIC, dateTime);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when
        final Long updatedMemoId = memoService.updateMemo(memoId, memoUpdateRequestDto);

        // then
        assertThat(updatedMemoId).isEqualTo(memoId);
        assertThat(mockMemo.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("갱신하려는 메모 아이디가 존재하지 않으면 IllegalArgumentException이 발생한다.")
    @Test
    void 갱신하려는_메모가_존재하지_않으면_예외가_발생한다() {
        // given
        Long invalidId = 1L;
        doReturn(Optional.empty()).when(memoRepository).findById(invalidId);

        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("테스트", MemoScope.PUBLIC, dateTime);

        // when, then
        assertThatThrownBy(() -> memoService.updateMemo(invalidId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 게시글이 없습니다.");
    }

    @DisplayName("메모 아이디를 받아 메모를 삭제한다.")
    @Test
    void 메모_삭제_테스트() {
        // given
        Long memoId = 1L;
        Memo memo = createMemo(MemoScope.PUBLIC, "삭제 테스트");
        doReturn(Optional.of(memo)).when(memoRepository).findById(memoId);

        // when
        memoService.deleteMemo(memoId);

        // then
        verify(memoRepository).delete(memo);
    }

    @DisplayName("삭제하려는 메모 아이디가 존재하지 않으면 IllegalArgumentException이 발생한다.")
    @Test
    void 삭제_아이디가_존재하지_않으면_예외가_발생한다() {
        // given
        Long invalidId = 1L;

        doReturn(Optional.empty()).when(memoRepository).findById(invalidId);

        // when, then
        assertThatThrownBy(() -> memoService.deleteMemo(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 게시글이 없습니다.");
    }

    private Memo createMemo(MemoScope scope, String content) {
        return Memo.builder()
                .user(new User())
                .scope(scope.name())
                .content(content)
                .memoDate(dateTime)
                .build();
    }

    private Memo createMemo(User user, MemoScope scope, String content) {
        return Memo.builder()
                .user(user)
                .scope(scope.name())
                .content(content)
                .memoDate(dateTime)
                .build();
    }
}
