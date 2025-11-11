package com.precourse.openMission.unit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.precourse.openMission.config.auth.LoginUserArgumentResolver;
import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.Role;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.exception.GlobalExceptionHandler;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.MemoController;
import com.precourse.openMission.web.dto.memo.MemoListResponseDto;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import com.precourse.openMission.web.dto.memo.MemoUpdateRequestDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MemoControllerTest {
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    static final LocalDateTime dateTime = LocalDateTime.of(2025, 2, 10, 12, 30);
    User user;
    User adminUser;

    @Mock
    private MemoService memoService;

    @Mock
    private HttpSession mockSession;

    @InjectMocks
    private MemoController memoController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        LoginUserArgumentResolver loginUserArgumentResolver =
                new LoginUserArgumentResolver(mockSession);

        mockMvc = MockMvcBuilders.standaloneSetup(memoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(loginUserArgumentResolver)
                .build();

        user = User.builder()
                .name("일반 사용자")
                .role(Role.USER)
                .email("user@test.com")
                .build();

        adminUser = User.builder()
                .name("관리자")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();
    }

    @DisplayName("메모를 저장했을 때, 해당 메모의 아이디를 반환하는지 확인한다.")
    @Test
    void 로그인_사용자_메모_저장_테스트() throws Exception {
        // given
        Long expectedMemoId = 10L;

        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        String content = "test";
        MemoScope scope = MemoScope.PUBLIC;
        MemoSaveRequestDto memoSaveRequestDto = new MemoSaveRequestDto(content, scope, dateTime);

        doReturn(expectedMemoId).when(memoService).saveMemo(any(MemoSaveRequestDto.class), eq(sessionUser));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(memoSaveRequestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/home/memos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedMemoId)));
    }

    @DisplayName("비로그인 사용자가 메모를 저장하려할 때, IllegalArgumentException이 발생한다.")
    @Test
    void 비로그인_사용자가_메모_저장시_예외가_발생한다() throws Exception {
        // given
        String content = "test";
        MemoScope scope = MemoScope.PUBLIC;
        MemoSaveRequestDto memoSaveRequestDto = new MemoSaveRequestDto(content, scope, dateTime);

        when(mockSession.getAttribute("user")).thenReturn(null);
        doThrow(new IllegalArgumentException()).when(memoService).saveMemo(any(MemoSaveRequestDto.class), eq(null));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(memoSaveRequestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/home/memos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("비로그인 사용자가 전체 메모를 조회하면, 전체공개 메모만 반환된다.")
    @Test
    void 비로그인_사용자_전체_메모_조회_테스트() throws Exception {
        // given
        when(mockSession.getAttribute("user")).thenReturn(null);

        Memo memo1 = createMemo(MemoScope.PUBLIC, user, "공개글 1");
        Memo memo2 = createMemo(MemoScope.SECRET, user, "비밀글 1");
        ReflectionTestUtils.setField(memo1, "id", 1L);
        ReflectionTestUtils.setField(memo2, "id", 2L);

        List<MemoListResponseDto> responseDtos = List.of(new MemoListResponseDto(memo1));
        doReturn(responseDtos).when(memoService).findAllDesc(null);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos")
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value(memo1.getContent()));
    }

    @DisplayName("로그인 사용자가 전체 메모를 조회하면, 전체공개 메모와 사용자의 나만보기 메모가 최신순으로 반환된다.")
    @Test
    void 로그인_사용자_전체_메모_조회_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Memo memo1 = createMemo(MemoScope.PUBLIC, user, "공개글 1");
        Memo memo2 = createMemo(MemoScope.SECRET, user, "비밀글 1");
        ReflectionTestUtils.setField(memo1, "id", 1L);
        ReflectionTestUtils.setField(memo2, "id", 2L);

        List<MemoListResponseDto> responseDtos = List.of(new MemoListResponseDto(memo1), new MemoListResponseDto(memo2));
        doReturn(responseDtos).when(memoService).findAllDesc(sessionUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos")
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value(memo1.getContent()))
                .andExpect(jsonPath("$[1].content").value(memo2.getContent()));
    }

    @DisplayName("관리자 사용자가 전체 메모를 조회하면, 전체공개 메모와 사용자의 나만보기 메모가 최신순으로 반환된다.")
    @Test
    void 관리자_전체_메모_조회_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Memo memo1 = createMemo(MemoScope.PUBLIC, user, "공개글 1");
        Memo memo2 = createMemo(MemoScope.SECRET, user, "비밀글 1");
        ReflectionTestUtils.setField(memo1, "id", 1L);
        ReflectionTestUtils.setField(memo2, "id", 2L);

        List<MemoListResponseDto> responseDtos = List.of(new MemoListResponseDto(memo1), new MemoListResponseDto(memo2));
        doReturn(responseDtos).when(memoService).findAllDesc(sessionUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos")
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value(memo1.getContent()))
                .andExpect(jsonPath("$[1].content").value(memo2.getContent()));
    }

    @DisplayName("비로그인 상태에서 메모 아이디로 특정 전체 공개 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 비로그인_사용자_특정_전체공개_메모_조회_테스트() throws Exception {
        // given
        when(mockSession.getAttribute("user")).thenReturn(null);

        Long targetId = 1L;
        String expectedContent = "전체 공개 메모 조회 테스트";

        Memo memo = createMemo(MemoScope.PUBLIC, user, expectedContent);
        ReflectionTestUtils.setField(memo, "id", 1L);
        MemoResponseDto responseDto = new MemoResponseDto(memo);
        doReturn(responseDto).when(memoService).findById(targetId, null);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{targetId}", targetId)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(expectedContent))
                .andExpect(jsonPath("$.memoId").value(targetId));
    }

    @DisplayName("로그인 상태에서 메모 아이디로 전체 공개 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 로그인_사용자_특정_전체공개_메모_조회_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long targetId = 1L;
        String expectedContent = "전체 공개 메모 조회 테스트";

        Memo memo = createMemo(MemoScope.PUBLIC, user, expectedContent);
        ReflectionTestUtils.setField(memo, "id", 1L);
        MemoResponseDto responseDto = new MemoResponseDto(memo);
        doReturn(responseDto).when(memoService).findById(targetId, sessionUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{targetId}", targetId)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(expectedContent))
                .andExpect(jsonPath("$.memoId").value(targetId));
    }

    @DisplayName("로그인 상태에서 메모 아이디로 내 나만보기 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 로그인_사용자_본인_나만보기_메모_조회_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long targetId = 1L;
        String expectedContent = "내 나만보기 메모 조회 테스트";

        Memo memo = createMemo(MemoScope.SECRET, user, expectedContent);
        ReflectionTestUtils.setField(memo, "id", 1L);
        MemoResponseDto responseDto = new MemoResponseDto(memo);
        doReturn(responseDto).when(memoService).findById(targetId, sessionUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{targetId}", targetId)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(expectedContent))
                .andExpect(jsonPath("$.memoId").value(targetId));
    }

    @DisplayName("관리자가 메모 아이디로 타인의 나만보기 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 관리자_타인_나만보기_메모_조회_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long targetId = 1L;
        String expectedContent = "남의 나만보기 메모 조회 테스트";

        Memo memo = createMemo(MemoScope.SECRET, user, expectedContent);
        ReflectionTestUtils.setField(memo, "id", 1L);
        MemoResponseDto responseDto = new MemoResponseDto(memo);
        doReturn(responseDto).when(memoService).findById(targetId, sessionUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{targetId}", targetId)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(expectedContent))
                .andExpect(jsonPath("$.memoId").value(targetId));
    }

    @DisplayName("로그인 사용자가 메모 아이디로 타인의 나만보기 메모를 조회했을 때, 400이 응답된다..")
    @Test
    void 로그인_사용자_타인_나만보기_메모_조회_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long memoId = 1L;
        String expectedContent = "남의 나만보기 메모 조회 테스트";

        Memo memo = createMemo(MemoScope.SECRET, adminUser, expectedContent);
        ReflectionTestUtils.setField(memo, "id", 1L);
        MemoResponseDto responseDto = new MemoResponseDto(memo);
        doThrow(new IllegalArgumentException()).when(memoService).findById(memoId, sessionUser);

        // when, then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{targetId}", memoId)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("비로그인 사용자가 남의 나만보기 메모를 조회했을 때, 400이 응답된다.")
    @Test
    void 비로그인_사용자가_남의_나만보기_메모_조회시_예외가_발생한다() throws Exception {
        // given
        Long memoId = 1L;
        when(mockSession.getAttribute("user")).thenReturn(null);
        doThrow(new IllegalArgumentException()).when(memoService).findById(memoId, null);

        // when, then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{memoId}", memoId)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 조회했을 때, 400이 응답된다.")
    @Test
    void 존재하지_않는_메모_조회시_예외가_발생한다() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long invalidId = 1L;
        doThrow(new IllegalArgumentException()).when(memoService).findById(eq(invalidId), any(SessionUser.class));

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{invalidID}", invalidId)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("로그인한 사용자의 메모 아이디와 MemoUpdateRequestDto(공개범위, 내용, 날짜)로 메모를 갱신하고, 200을 반환한다.")
    @Test
    void 로그인_사용자_본인_메모_갱신_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long memoId = 1L;
        String updatedContent = "갱신 후";
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(updatedContent, MemoScope.PUBLIC, dateTime);
        doReturn(memoId).when(memoService).updateMemo(eq(memoId), any(MemoUpdateRequestDto.class), eq(sessionUser));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/home/memos/{memoID}", memoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(memoId)));
    }

    @DisplayName("관리자가 메모 아이디와 MemoUpdateRequestDto(공개범위, 내용, 날짜)로 메모를 갱신하면, 200을 반환한다.")
    @Test
    void 관리자_타인_메모_갱신_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long memoId = 1L;
        String updatedContent = "갱신 후";
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(updatedContent, MemoScope.PUBLIC, dateTime);
        doReturn(memoId).when(memoService).updateMemo(eq(memoId), any(MemoUpdateRequestDto.class), eq(sessionUser));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/home/memos/{memoID}", memoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(memoId)));
    }

    @DisplayName("로그인한 사용자가 타인의 메모를 갱신하려했을 때, 400이 응답된다.")
    @Test
    void 로그인_사용자가_타인_메모_갱신시_예외가_발생한다() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long memoId = 1L;
        String updatedContent = "갱신 후";
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(updatedContent, MemoScope.PUBLIC, dateTime);
        doThrow(new IllegalArgumentException()).when(memoService).updateMemo(eq(memoId), any(MemoUpdateRequestDto.class), eq(sessionUser));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/home/memos/{memoId}", memoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("비로그인 사용자가 타인의 메모를 갱신하려했을 때, 400이 응답된다.")
    @Test
    void 비로그인_사용자가_타인_메모_갱신시_예외가_발생한다() throws Exception {
        // given
        when(mockSession.getAttribute("user")).thenReturn(null);

        Long memoId = 1L;
        String updatedContent = "갱신 후";
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(updatedContent, MemoScope.PUBLIC, dateTime);
        doThrow(new IllegalArgumentException()).when(memoService).updateMemo(eq(memoId), any(MemoUpdateRequestDto.class), eq(null));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/home/memos/{memoId}", memoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 갱신하려했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    void 존재하지_않는_메모_갱신시_예외가_발생한다() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long invalidId = 1L;
        String updatedContent = "갱신 후";
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(updatedContent, MemoScope.PUBLIC, dateTime);
        doThrow(new IllegalArgumentException()).when(memoService).updateMemo(eq(invalidId), any(MemoUpdateRequestDto.class), any(SessionUser.class));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/home/memos/{invalidId}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("로그인한 사용자의 메모 아이디를 받아 해당 메모를 삭제한다.")
    @Test
    void 로그인_사용자_본인_메모_삭제_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long memoId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/memos/{memoId}", memoId)
        );

        // then
        resultActions.andExpect(status().isNoContent());
        verify(memoService).deleteMemo(memoId, sessionUser);
    }

    @DisplayName("로그인한 사용자의 메모 아이디를 받아 해당 메모를 삭제한다.")
    @Test
    void 관리자_타인_메모_삭제_테스트() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(adminUser);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long memoId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/memos/{memoId}", memoId)
        );

        // then
        resultActions.andExpect(status().isNoContent());
        verify(memoService).deleteMemo(memoId, sessionUser);
    }

    @DisplayName("관리자가 아닌 일반 로그인 사용자가 타인의 메모를 삭제하려하면 예외가 발생한다.")
    @Test
    void 로그인_사용자가_타인_메모_삭제시_예외가_발생한다() throws Exception {
        // given
        Long othersMemoId = 1L;
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);
        doThrow(new IllegalArgumentException()).when(memoService).deleteMemo(othersMemoId, sessionUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/memos/{memoId}", othersMemoId)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("비로그인 사용자가 메모를 삭제하려하면 예외가 발생한다.")
    @Test
    void 비로그인_사용자가_메모_삭제시_예외가_발생한다() throws Exception {
        // give
        Long memoId = 1L;
        when(mockSession.getAttribute("user")).thenReturn(null);
        doThrow(new IllegalArgumentException()).when(memoService).deleteMemo(memoId, null);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/memos/{memoId}", memoId)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 삭제했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    void 존재하지_않는_메모_삭제시_예외가_발생한다() throws Exception {
        // given
        SessionUser sessionUser = new SessionUser(user);
        when(mockSession.getAttribute("user")).thenReturn(sessionUser);

        Long invalidId = 1L;
        doThrow(new IllegalArgumentException()).when(memoService).deleteMemo(eq(invalidId), any(SessionUser.class));

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/memos/{invalidId}", invalidId)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    private Memo createMemo(MemoScope scope, User user, String content) {
        return Memo.builder()
                .user(user)
                .memoDate(dateTime)
                .content(content)
                .scope(scope.name())
                .build();
    }
}
