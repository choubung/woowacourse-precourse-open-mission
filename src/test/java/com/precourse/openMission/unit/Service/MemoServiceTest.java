package com.precourse.openMission.unit.Service;

import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.Role;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import com.precourse.openMission.web.dto.memo.MemoUpdateRequestDto;
import org.junit.jupiter.api.BeforeEach;
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
    User user;
    User adminUser;

    @Mock
    private MemoRepository memoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemoService memoService;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("사용자")
                .email("user@test.com")
                .role(Role.USER)
                .build();

        adminUser = User.builder()
                .name("관리자")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void 로그인_사용자_메모_저장_테스트(){
        // given
        Long expectedMemoId = 10L;

        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        String content = "test";
        MemoScope scope = MemoScope.PUBLIC;
        MemoSaveRequestDto memoSaveRequestDto = new MemoSaveRequestDto(content, scope, dateTime);

        Memo memo = createMemo(user, scope, content);
        ReflectionTestUtils.setField(memo, "id", 10L);
        doReturn(memo).when(memoRepository).save(any(Memo.class));

        // when
        final Long savedMemoId = memoService.saveMemo(memoSaveRequestDto,sessionUser);

        // then
        assertThat(savedMemoId).isEqualTo(expectedMemoId);
    }

    @DisplayName("비로그인 사용자가 메모 저장시 IllegalArgumentException이 발생한다.")
    @Test
    void 비로그인_사용자가_메모_저장시_예외가_발생한다(){
        // given
        String content = "test";
        MemoScope scope = MemoScope.PUBLIC;
        MemoSaveRequestDto memoSaveRequestDto = new MemoSaveRequestDto(content, scope, dateTime);

        // when, then
        assertThatThrownBy(() -> memoService.saveMemo(memoSaveRequestDto, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("비로그인 사용자가 전체 공개 메모를 조회한다.")
    @Test
    void 비로그인_사용자_전체_메모_조회_테스트(){
        // given
        SessionUser sessionUser = null;

        Memo memo1 = createMemo(user, MemoScope.PUBLIC, "공개글 1");

        List<Memo> mockPublicMemos = List.of(memo1);
        doReturn(mockPublicMemos).when(memoRepository).findAllPublicDesc();

        // when
        final List<MemoListResponseDto> memoListResponseDtos = memoService.findAllDesc(sessionUser);

        // then
        assertThat(memoListResponseDtos).hasSize(1);
        assertThat(memoListResponseDtos.get(0).getContent()).isEqualTo("공개글 1");
    }

    @DisplayName("로그인 사용자가 전체 메모을 조회하면, 모든 전체 공개 메모와 로그인 사용자의 나만보기 메모가 조회된다.")
    @Test
    void 로그인_사용자_전체_메모_조회_테스트(){
        // given
        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        Memo memo1 = createMemo(user, MemoScope.PUBLIC, "공개글 1");
        Memo memo2 = createMemo(user, MemoScope.SECRET, "비밀글 1");

        List<Memo> mockPublicMemos = List.of(memo1, memo2);
        doReturn(mockPublicMemos).when(memoRepository).findAllPublicAndMySecretDesc(user);

        // when
        final List<MemoListResponseDto> memoListResponseDtos = memoService.findAllDesc(sessionUser);

        // then
        assertThat(memoListResponseDtos).hasSize(2);
    }

    @DisplayName("관리자가 전체 메모을 조회하면, 모든 전체 공개 메모와 모든 나만보기 메모가 조회된다.")
    @Test
    void 관리자_전체_메모_조회_테스트(){
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        doReturn(Optional.of(adminUser)).when(userRepository).findByEmail(sessionUser.getEmail());

        Memo memo1 = createMemo(user, MemoScope.PUBLIC, "공개글 1");
        Memo memo2 = createMemo(user, MemoScope.SECRET, "비밀글 1");

        List<Memo> mockMemos = List.of(memo1, memo2);
        doReturn(mockMemos).when(memoRepository).findAllDesc();

        // when
        final List<MemoListResponseDto> memoListResponseDtos = memoService.findAllDesc(sessionUser);

        // then
        assertThat(memoListResponseDtos).hasSize(2);
    }

    @DisplayName("비로그인 상태에서 메모 아이디로 특정 전체 공개 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 비로그인_사용자_특정_전체공개_메모_조회_테스트(){
        // given
        Long memoId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(user, MemoScope.PUBLIC, expectedContent);
        ReflectionTestUtils.setField(mockMemo, "id", 1L);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when
        final MemoResponseDto memoResponseDto = memoService.findById(memoId, null);

        // then
        assertThat(memoResponseDto).isNotNull();
        assertThat(memoResponseDto.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("로그인 상태에서 메모 아이디로 특정 전체 공개 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 로그인_사용자_특정_전체공개_메모_조회_테스트(){
        // given
        SessionUser sessionUser = new SessionUser(user);

        Long targetId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(user, MemoScope.PUBLIC, expectedContent);
        ReflectionTestUtils.setField(mockMemo, "id", 1L);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(targetId);

        // when
        final MemoResponseDto memoResponseDto = memoService.findById(targetId, sessionUser);

        // then
        assertThat(memoResponseDto).isNotNull();
        assertThat(memoResponseDto.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("로그인 사용자가 메모 아이디로 본인의 특정 나만보기 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 로그인_사용자_본인_특정_나만보기_메모_조회_테스트(){
        // given
        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(user, MemoScope.SECRET, expectedContent);
        ReflectionTestUtils.setField(mockMemo, "id", 1L);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when
        final MemoResponseDto memoResponseDto = memoService.findById(memoId, sessionUser);

        // then
        assertThat(memoResponseDto).isNotNull();
        assertThat(memoResponseDto.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("관리자가 메모 아이디로 타인의 특정 나만보기 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 관리자_타인_특정_나만보기_메모_조회_테스트(){
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        doReturn(Optional.of(adminUser)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(user, MemoScope.SECRET, expectedContent);
        ReflectionTestUtils.setField(mockMemo, "id", 1L);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when
        final MemoResponseDto memoResponseDto = memoService.findById(memoId, sessionUser);

        // then
        assertThat(memoResponseDto).isNotNull();
        assertThat(memoResponseDto.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("비로그인 사용자가 메모 아이디로 타인의 특정 나만보기 메모를 조회했을 때, 예외가 발생한다.")
    @Test
    void 비로그인_사용자가_타인_특정_나만보기_메모_조회시_예외가_발생한다(){
        // given
        Long memoId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(user, MemoScope.SECRET, expectedContent);
        ReflectionTestUtils.setField(mockMemo, "id", 1L);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when, then
        assertThatThrownBy(()-> memoService.findById(memoId, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("로그인 사용자가 메모 아이디로 타인의 특정 나만보기 메모를 조회했을 때, 예외가 발생한다.")
    @Test
    void 로그인_사용자가_타인_특정_나만보기_메모_조회시_예외가_발생한다(){
        // given
        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        String expectedContent = "테스트 내용";

        Memo mockMemo = createMemo(adminUser, MemoScope.SECRET, expectedContent);
        ReflectionTestUtils.setField(mockMemo, "id", 1L);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when, then
        assertThatThrownBy(()-> memoService.findById(memoId, sessionUser))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 조회했을 때, IllegalArgumentException이 발생한다.")
    @Test
    void 존재하지_않는_메모_조회시_예외가_발생한다() {
        // given
        Long invalidId = 1L;

        doReturn(Optional.empty()).when(memoRepository).findById(invalidId);

        // when, then
        assertThatThrownBy(() -> memoService.findById(invalidId, new SessionUser(user)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("로그인한 사용자의 메모 아이디와 MemoUpdateRequestDto(공개범위, 내용, 날짜)를 받아 메모를 업데이트한다.")
    @Test
    void 로그인_사용자_본인_메모_갱신_테스트() {
        // given
        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        String expectedContent = "업데이트 후";

        Memo mockMemo = createMemo(user, MemoScope.PUBLIC, "업데이트 전");
        ReflectionTestUtils.setField(mockMemo, "id", memoId);
        MemoUpdateRequestDto memoUpdateRequestDto = new MemoUpdateRequestDto(expectedContent, MemoScope.PUBLIC, dateTime);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when
        final Long updatedMemoId = memoService.updateMemo(memoId, memoUpdateRequestDto, sessionUser);

        // then
        assertThat(updatedMemoId).isEqualTo(memoId);
        assertThat(mockMemo.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("관리자가 특정 메모 아이디와 MemoUpdateRequestDto(공개범위, 내용, 날짜)로 메모를 업데이트한다.")
    @Test
    void 관리자_타인_메모_갱신_테스트() {
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        doReturn(Optional.of(adminUser)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        String expectedContent = "업데이트 후";

        Memo mockMemo = createMemo(user, MemoScope.PUBLIC, "업데이트 전");
        ReflectionTestUtils.setField(mockMemo, "id", memoId);
        MemoUpdateRequestDto memoUpdateRequestDto = new MemoUpdateRequestDto(expectedContent, MemoScope.PUBLIC, dateTime);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when
        final Long updatedMemoId = memoService.updateMemo(memoId, memoUpdateRequestDto, sessionUser);

        // then
        assertThat(updatedMemoId).isEqualTo(memoId);
        assertThat(mockMemo.getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("로그인 사용자가 타인의 특정 메모 갱신시 예외가 발생한다.")
    @Test
    void 로그인_사용자가_타인_메모_갱신시_예외가_발생한다() {
        // given
        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        String expectedContent = "업데이트 후";

        Memo mockMemo = createMemo(adminUser, MemoScope.PUBLIC, "업데이트 전");
        ReflectionTestUtils.setField(mockMemo, "id", memoId);
        MemoUpdateRequestDto memoUpdateRequestDto = new MemoUpdateRequestDto(expectedContent, MemoScope.PUBLIC, dateTime);
        doReturn(Optional.of(mockMemo)).when(memoRepository).findById(memoId);

        // when, then
        assertThatThrownBy(()-> memoService.updateMemo(memoId, memoUpdateRequestDto, sessionUser))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("비로그인 사용자가 타인의 특정 메모 갱신시 예외가 발생한다.")
    @Test
    void 비로그인_사용자가_타인_메모_갱신시_예외가_발생한다() {
        // given
        Long memoId = 1L;
        MemoUpdateRequestDto memoUpdateRequestDto = new MemoUpdateRequestDto("업데이트 후", MemoScope.PUBLIC, dateTime);

        // when, then
        assertThatThrownBy(()-> memoService.updateMemo(memoId, memoUpdateRequestDto, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 갱신하려했을 때, IllegalArgumentException이 발생한다.")
    @Test
    void 존재하지_않는_메모_갱신시_예외가_발생한다() {
        // given
        Long invalidId = 1L;
        doReturn(Optional.empty()).when(memoRepository).findById(invalidId);

        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("테스트", MemoScope.PUBLIC, dateTime);

        // when, then
        assertThatThrownBy(() -> memoService.updateMemo(invalidId, requestDto, new SessionUser(user)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("로그인한 사용자의 메모 아이디를 받아 해당 메모를 삭제한다.")
    @Test
    void 로그인_사용자_본인_메모_삭제_테스트() {
        // given
        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        Memo memo = createMemo(user, MemoScope.PUBLIC, "삭제 테스트");
        doReturn(Optional.of(memo)).when(memoRepository).findById(memoId);

        // when
        memoService.deleteMemo(memoId, sessionUser);

        // then
        verify(memoRepository).delete(memo);
    }

    @DisplayName("관리자가 메모 아이디로 타인의 메모를 삭제한다.")
    @Test
    void 관리자_타인_메모_삭제_테스트() {
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        doReturn(Optional.of(adminUser)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        Memo memo = createMemo(user, MemoScope.PUBLIC, "삭제 테스트");
        doReturn(Optional.of(memo)).when(memoRepository).findById(memoId);

        // when
        memoService.deleteMemo(memoId, sessionUser);

        // then
        verify(memoRepository).delete(memo);
    }

    @DisplayName("비로그인 사용자가 메모 아이디로 타인의 메모 삭제시 예외가 발생한다.")
    @Test
    void 비로그인_사용자가_타인_메모_삭제시_예외가_발생한다() {
        // given
        Long memoId = 1L;

        // when, then
        assertThatThrownBy(() -> memoService.deleteMemo(memoId, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("로그인 사용자가 메모 아이디로 타인의 메모 삭제시 예외가 발생한다.")
    @Test
    void 로그인_사용자가_타인_메모_삭제시_예외가_발생한다() {
        // given
        SessionUser sessionUser = new SessionUser(user);
        doReturn(Optional.of(user)).when(userRepository).findByEmail(sessionUser.getEmail());

        Long memoId = 1L;
        Memo memo = createMemo(adminUser, MemoScope.PUBLIC, "삭제 테스트");
        doReturn(Optional.of(memo)).when(memoRepository).findById(memoId);

        // when, then
        assertThatThrownBy(() -> memoService.deleteMemo(memoId, sessionUser))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("삭제하려는 메모 아이디가 존재하지 않으면 IllegalArgumentException이 발생한다.")
    @Test
    void 삭제_아이디가_존재하지_않으면_예외가_발생한다() {
        // given
        Long invalidId = 1L;

        doReturn(Optional.empty()).when(memoRepository).findById(invalidId);

        // when, then
        assertThatThrownBy(() -> memoService.deleteMemo(invalidId, new SessionUser(user)))
                .isInstanceOf(IllegalArgumentException.class);
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
