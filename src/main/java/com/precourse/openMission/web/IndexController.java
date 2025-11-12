package com.precourse.openMission.web;

import com.precourse.openMission.config.auth.LoginUser;
import com.precourse.openMission.config.auth.dto.SessionUser;
import com.precourse.openMission.domain.memo.MemoScope;
import com.precourse.openMission.service.MemoService;
import com.precourse.openMission.web.dto.memo.MemoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final MemoService memoService;

    /**
     * 메인 페이지 (목록 조회)
     */
    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        model.addAttribute("memos", memoService.findAllDesc(user));

        if (user != null) {
            model.addAttribute("googleName", user.getName());
        }

        return "index";
    }

    /**
     * 메모 등록 페이지
     */
    @GetMapping("/home/memos/save")
    public String memosSave(Model model, @LoginUser SessionUser user) {
        // 헤더에 사용자 이름을 표시하기 위해 Model에 googleName 추가
        if (user != null) {
            model.addAttribute("googleName", user.getName());
        }
        return "memos-save";
    }

    /**
     * 💡 [추가된 메서드]
     * 메모 상세 조회 페이지
     */
    @GetMapping("/home/memos/detail/{memoId}")
    public String memosDetail(@PathVariable Long memoId, Model model, @LoginUser SessionUser user) {

        // 1. 서비스 호출: memoId와 user 정보로 메모 조회 (권한 검사 포함)
        MemoResponseDto dto = memoService.findById(memoId, user);

        // 2. 뷰(memos-detail.mustache)로 전달할 "memo" 객체를 Model에 추가
        model.addAttribute("memo", dto);

        // 3. 헤더(header.mustache)에서 사용할 로그인 사용자 이름
        if (user != null) {
            model.addAttribute("googleName", user.getName());
        }

        // 4. "memos-detail.mustache" 뷰 파일을 렌더링
        return "memos-detail";
    }


    /**
     * 메모 수정 페이지
     * (이 메서드는 /home/memos/update/{id} 경로를 처리합니다)
     */
    @GetMapping("/home/memos/update/{id}")
    public String memosUpdate(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        // 1. 서비스에서 Memo 상세 정보를 가져옴 (권한 검사 포함)
        MemoResponseDto dto = memoService.findById(id, user);
        model.addAttribute("memo", dto);

        // 2. 헤더에 사용자 이름 표시
        if (user != null) {
            model.addAttribute("googleName", user.getName());
        }

        // 3. DTO의 scope에 따라 'checked' 속성을 위한 boolean 값 추가
        if (dto.getScope() == MemoScope.PUBLIC) {
            model.addAttribute("isPublic", true);
        } else {
            model.addAttribute("isSecret", true);
        }

        // 4. datetime-local input은 'YYYY-MM-DDTHH:mm' 형식이 필요
        if (dto.getMemoDate() != null) {
            model.addAttribute("memoDateFormatted",
                    dto.getMemoDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        }

        return "memos-update";
    }
}