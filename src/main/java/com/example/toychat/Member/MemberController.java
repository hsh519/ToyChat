package com.example.toychat.Member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/member/join")
    public String joinForm() {
        return "member/joinForm";
    }

    @PostMapping("/member/join")
    public String join(@RequestParam String username) {
        memberRepository.save(new Member(username));
        return "redirect:/member/login";
    }

    @GetMapping("/member/login")
    public String loginForm() {
        return "member/loginForm";
    }

    @PostMapping("/member/login")
    public String login(@RequestParam String username, HttpServletRequest request) {
        Member findMember = memberRepository.findByUsername(username);
        HttpSession session = request.getSession();
        session.setAttribute("loginMember", findMember);
        return "redirect:/member/list";
    }

    @GetMapping("/member/list")
    public String memberList(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Member loginMember = (Member) session.getAttribute("loginMember");

        List<Member> list = memberRepository.findMemberList(loginMember.getId());
        model.addAttribute("list", list);
        return "member/list";
    }
}
