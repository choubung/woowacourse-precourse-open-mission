package com.precourse.openMission.unit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.exception.GlobalExceptionHandler;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.MemoController;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MemoControllerTest {
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    static final LocalDateTime dateTime = LocalDateTime.of(2025, 2, 10, 12, 30);

    @Mock
    private MemoService memoService;

    @InjectMocks
    private MemoController memoController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memoController).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @DisplayName("메모를 저장했을 때, 해당 메모의 아이디를 반환하는지 확인한다.")
    @Test
    void 메모_저장_테스트() throws Exception {
        // given
        Long expectedMemoId = 10L;

        String content = "test";
        Long userId = 1L;
        MemoScope scope = MemoScope.PUBLIC;
        MemoSaveRequestDto memoSaveRequestDto = new MemoSaveRequestDto(content, userId, scope, dateTime);
        doReturn(expectedMemoId).when(memoService).saveMemo(any(MemoSaveRequestDto.class));

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

    @DisplayName("전체 메모를 조회했을 때, 리스트가 잘 넘어오는지 확인한다.")
    @Test
    void 전체_메모_조회_테스트() throws Exception {
        // given
        Memo memo1 = createMemo(MemoScope.PUBLIC, "공개글 1");
        Memo memo2 = createMemo(MemoScope.PUBLIC, "공개글 2");
        ReflectionTestUtils.setField(memo1, "id", 1L);
        ReflectionTestUtils.setField(memo2, "id", 2L);

        List<MemoListResponseDto> responseDtos = List.of(new MemoListResponseDto(memo1), new MemoListResponseDto(memo2));
        doReturn(responseDtos).when(memoService).findAllPublicDesc();

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos")
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(memo1.getContent()))
                .andExpect(jsonPath("$[1].content").value(memo2.getContent()));
    }

    @DisplayName("메모 아이디로 메모를 조회했을 때, 해당 메모의 데이터가 넘어오는지 확인한다.")
    @Test
    void 아이디로_메모_조회_테스트() throws Exception {
        // given
        Long targetId = 1L;
        String expectedContent = "아이디 게시글 조회 테스트";

        Memo memo = createMemo(MemoScope.PUBLIC, expectedContent);
        ReflectionTestUtils.setField(memo, "id", 1L);
        MemoResponseDto responseDto = new MemoResponseDto(memo);
        doReturn(responseDto).when(memoService).findById(targetId);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/1")
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(expectedContent))
                .andExpect(jsonPath("$.memoId").value(targetId));
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 조회했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    void 존재하지_않는_메모_조회시_예외가_발생한다() throws Exception {
        // given
        Long invalidId = 1L;
        doThrow(new IllegalArgumentException()).when(memoService).findById(invalidId);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/home/memos/{invalidID}", invalidId)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("메모 아이디와 MemoUpdateRequestDto(공개범위, 내용, 날짜)를 받아 메모를 갱신하고, 200을 반환한다.")
    @Test
    void 메모_갱신_테스트() throws Exception {
        // given
        Long memoId = 1L;
        String updatedContent = "갱신 후";
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(updatedContent, MemoScope.PUBLIC, dateTime);
        doReturn(memoId).when(memoService).updateMemo(eq(memoId), any(MemoUpdateRequestDto.class));

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

    @DisplayName("존재하지 않는 메모 아이디로 메모를 갱신하려했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    void 존재하지_않는_메모_갱신시_예외가_발생한다() throws Exception {
        // given
        Long invalidId = 1L;
        String updatedContent = "갱신 후";
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(updatedContent, MemoScope.PUBLIC, dateTime);
        doThrow(new IllegalArgumentException()).when(memoService).updateMemo(eq(invalidId), any(MemoUpdateRequestDto.class));

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

    @DisplayName("메모 아이디를 받아 해당 메모를 삭제한다.")
    @Test
    void 메모_삭제_테스트() throws Exception {
        // given
        Long memoId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/memos/{memoId}", memoId)
        );

        // then
        resultActions.andExpect(status().isNoContent());
        verify(memoService).deleteMemo(memoId);
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 삭제했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    void 존재하지_않는_메모_삭제시_예외가_발생한다() throws Exception {
        // given
        Long invalidId = 1L;
        doThrow(new IllegalArgumentException()).when(memoService).deleteMemo(invalidId);

        // when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/home/memos/{invalidId}", invalidId)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    private Memo createMemo(MemoScope scope, String content) {
        return Memo.builder()
                .user(new User())
                .memoDate(dateTime)
                .content(content)
                .scope(String.valueOf(scope))
                .build();
    }
}
