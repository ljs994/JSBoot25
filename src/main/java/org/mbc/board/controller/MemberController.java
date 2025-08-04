package org.mbc.board.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.board.domain.Member;
import org.mbc.board.dto.MemberJoinDTO;
import org.mbc.board.dto.MemberModifyDTO;
import org.mbc.board.repository.MemberRepository;
import org.mbc.board.security.dto.MemberSecurityDTO;
import org.mbc.board.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/member")
@Log4j2
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService; // 데이터베이스까지 엔티티와 dto를 처리
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public void loginGet(String error, String logout){
        // http://localhost/member/login?error=???
        // http://localhost/member/login?logout=???
        log.info("MemberController.loginGet메서드 실행....");
        log.info("logout: " + logout); //데이터베이스에서 활용
        log.info("error: " + error); //데이터베이스에서 활용

        if(logout != null){
            log.info("logout 처리됨!!! : " + logout);
        }

    }// 로그인메서드 종료

    @GetMapping("/join")  // http://localhost/member/join
    public void joinGet(){
        // void -> templates/member/join.html
        log.info("MemberController.joinGet..... ");
        // url이 넘어오면 프론트로 페이지 출력용
    }
    
    @PostMapping("/join") // http://localhost/member/join post메서드로 처리됨
    public String joinPost(MemberJoinDTO memberJoinDTO, RedirectAttributes redirectAttributes){
    // html에서 넘어오는 데이터 처리용
    
        log.info("MemberController.joinPost..... "); 
        log.info(memberJoinDTO);

        try{
            memberService.join(memberJoinDTO); // 회원가입 처리됨!!
        }catch(MemberService.MidExistException e){

            redirectAttributes.addFlashAttribute("error","mid");
            // 회원가입시 id 중복되는 예외처리
            return "redirect:/member/join"; // 회원가입 페이지로 다시 감.

        }
        redirectAttributes.addFlashAttribute("result","success");
        return "redirect:/member/login"; //회원가입 성공시 로그인 페이지로 이동

        // return "redirect:/board/list"; // 회원가입 후에 리스트 페이지로 이동
        
    }

    @GetMapping("/modify")
    public String modifyGet(@AuthenticationPrincipal MemberSecurityDTO memberDTO, Model model) {

        log.info("MemberController.ModifyGet 메서드 실행");
        log.info("Login user: " + memberDTO);

        // 로그인된 사용자 정보를 model에 추가 (타임리프에서 출력 가능)
        model.addAttribute("member", memberDTO);

        return "member/modify"; // templates/member/modify.html
    }

    @PostMapping("/modify")
    public String modifyPost(@AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
                                 MemberModifyDTO memberModifyDTO, RedirectAttributes redirectAttributes) {

        log.info("비밀번호 수정 요청: " + memberModifyDTO.getMpw());

        Optional<Member> result = memberRepository.findByMid(memberSecurityDTO.getUsername());

        if (result.isPresent()) {
            Member member = result.get();

            member.changePassword(passwordEncoder.encode(memberModifyDTO.getMpw()));
            memberRepository.save(member);

            log.info("비밀번호 수정 완료");
        }

        return "redirect:/board/list";
    }

}
