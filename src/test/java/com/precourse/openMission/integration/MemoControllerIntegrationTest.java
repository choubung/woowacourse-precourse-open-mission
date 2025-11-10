package com.precourse.openMission.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.precourse.openMission.domain.memo.Memo;
import com.precourse.openMission.domain.memo.MemoRepository;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.domain.user.Role;
import com.precourse.openMission.domain.user.User;
import com.precourse.openMission.domain.user.UserRepository;
import com.precourse.openMission.web.dto.memo.MemoSaveRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MemoControllerIntegrationTest {
    private User user;

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
        mockMvc.perform(post("/home/memos")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());

        //then
        List<Memo> memos = memoRepository.findAll();
        assertThat(memos).hasSize(1);

        Memo memo = memos.get(0);
        assertThat(memo.getContent()).isEqualTo(content);
        assertThat(memo.getScope()).isEqualTo(scope);
        assertThat(memo.getMemoDate()).isEqualTo(date);
    }
}
