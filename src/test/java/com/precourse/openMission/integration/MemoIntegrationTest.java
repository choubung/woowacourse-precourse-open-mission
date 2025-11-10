package com.precourse.openMission.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.Role;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import com.precourse.openMission.web.dto.memo.MemoUpdateRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MemoIntegrationTest {
    private User user;
    private Memo publicMemo;
    private Memo secretMemo;

    @Autowired
    private MemoRepository memoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext).build();

        user = User.builder()
                .name("홍길동")
                .email("gildong@woowa.com")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        publicMemo = memoRepository.save(Memo.builder()
                .content("[공개글] 기존 테스트 데이터 1")
                .user(user)
                .scope(MemoScope.PUBLIC.name())
                .memoDate(LocalDateTime.now())
                .build()
        );

        secretMemo = memoRepository.save(Memo.builder()
                .content("[비밀글] 기존 테스트 데이터 2")
                .user(user)
                .scope(MemoScope.SECRET.name())
                .memoDate(LocalDateTime.now())
                .build()
        );
    }

    @AfterEach
    public void tearDown() throws Exception {
        memoRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void 메모_등록_통합_테스트() throws Exception {
        // given
        String content = "테스트";
        MemoScope scope = MemoScope.PUBLIC;
        LocalDateTime date = LocalDateTime.of(2025, 11, 10, 14, 57);

        MemoSaveRequestDto requestDto = new MemoSaveRequestDto(content, user.getId(), scope, date);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/home/memos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        MvcResult mvcResult = resultActions.andReturn();
        Long memoId = Long.parseLong(mvcResult.getResponse().getContentAsString());

        //then
        Memo memo = memoRepository.findById(memoId).orElse(null);
        assertThat(memo.getContent()).isEqualTo(content);
        assertThat(memo.getScope()).isEqualTo(scope);
        assertThat(memo.getMemoDate()).isEqualTo(date);
    }

    @Test
    public void 메모_갱신_통합_테스트() throws Exception {
        // given
        Long targetId = publicMemo.getId();
        String expectedContent = "업데이트된 test 내용";
        LocalDateTime expectedDate = LocalDateTime.of(2025, 11, 10, 14, 57);

        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(expectedContent, MemoScope.PUBLIC, expectedDate);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when
        mockMvc.perform(put("/home/memos/{targetId}", targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // then
        Optional<Memo> memo = memoRepository.findById(targetId);
        assertThat(memo.get().getContent()).isEqualTo(expectedContent);
        assertThat(memo.get().getMemoDate()).isEqualTo(expectedDate);
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 갱신하려했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    public void 존재하지_않는_메모_갱신시_예외가_발생한다() throws Exception {
        // given
        memoRepository.delete(publicMemo);
        Long invalidId = publicMemo.getId();

        String expectedContent = "업데이트된 test 내용";
        LocalDateTime expectedDate = LocalDateTime.of(2025, 11, 10, 14, 57);

        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto(expectedContent, MemoScope.PUBLIC, expectedDate);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        // when, then
        mockMvc.perform(put("/home/memos/{invalidId}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("전체 공개 메모를 모두 조회한다.")
    @Test
    public void 전체_메모_조회_통합_테스트() throws Exception {
        // given
        // 1. @BeforeEach에서 이미 공개글 1개, 비밀글 1개를 저장한 상태
        // 2. 공개글만 찾아야 함

        // when
        ResultActions resultActions = mockMvc.perform(get("/home/memos")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value("[공개글] 기존 테스트 데이터 1"));
    }

    @DisplayName("메모 아이디를 받아 해당 메모를 조회한다.")
    @Test
    public void 특정_메모_조회_통합_테스트() throws Exception {
        // given
        Long targetId = publicMemo.getId();
        String expectedContent = memoRepository.findById(targetId).get().getContent();

        // when
        ResultActions resultActions = mockMvc.perform(get("/home/memos/{targetId}", targetId));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.memoId").value(targetId))
                .andExpect(jsonPath("$.content").value(expectedContent));
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 조회했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    void 존재하지_않는_메모_조회시_예외가_발생한다() throws Exception {
        // given
        memoRepository.delete(publicMemo);
        Long invalidId = publicMemo.getId();

        // when, then
        mockMvc.perform(get("/home/memos/{invalidId}", invalidId))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("메모 아이디를 받아 해당 메모를 삭제한다.")
    @Test
    public void 메모_삭제_통합_테스트() throws Exception {
        // given
        Long targetId = publicMemo.getId();

        // when
        ResultActions resultActions = mockMvc.perform(delete("/home/memos/{targetId}", targetId));

        // then
        resultActions.andExpect(status().isNoContent());
        assertThat(memoRepository.findById(targetId)).isEqualTo(Optional.empty());
    }

    @DisplayName("존재하지 않는 메모 아이디로 메모를 삭제했을 때, 400 Error를 반환하는지 확인한다.")
    @Test
    void 존재하지_않는_메모_삭제시_예외가_발생한다() throws Exception {
        // given
        memoRepository.delete(publicMemo);
        Long invalidId = publicMemo.getId();

        // when, then
        mockMvc.perform(delete("/home/memos/{invalidId}", invalidId))
                .andExpect(status().isBadRequest());
    }
}
