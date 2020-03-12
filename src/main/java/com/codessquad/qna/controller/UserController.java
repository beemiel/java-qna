package com.codessquad.qna.controller;

import com.codessquad.qna.domain.User;
import com.codessquad.qna.domain.UserRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
@RequestMapping("/users") // 중복되는 prefix, 자원에 대해 명시해준다.
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/form")
    public String signUpFrom() {
        LOGGER.debug("[page] : {}", "회원가입 폼");
        return "users/form";
    }

    @PostMapping("")
    public String createUser(User user) {
        LOGGER.debug("[page] : {}", "사용자 생성");

        User createdUser = Optional.ofNullable(user).orElseThrow(() -> new NullPointerException("NULL"));
        userRepository.save(createdUser);

        return "redirect:/users/list";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        LOGGER.debug("[page] : {}", "로그인 폼");
        return "users/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        LOGGER.debug("[page] : {}", "로그아웃");
        session.removeAttribute(HttpSessionUtils.USER_SESSION_KEY);
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(String userId, String password, HttpSession session) throws NotFoundException {
        LOGGER.debug("[page] : {}", "로그인");

        User user = userRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if(!user.matchPassword(password)) {
            LOGGER.debug("[page] : {}", "비밀번호 불일치");
            return "redirect:/users/loginForm";
        }

        LOGGER.debug("[page] : {}", "로그인 성공");
        session.setAttribute(HttpSessionUtils.USER_SESSION_KEY, user);

        return "redirect:/";
    }

    @GetMapping("/list")
    public String userList(Model model) {
        LOGGER.debug("[page] : {}", "사용자 리스트");
        model.addAttribute("users", userRepository.findAll());
        return "users/list";
    }

    @GetMapping("/{id}")
    public String userProfile(@PathVariable Long id, Model model) throws NotFoundException {
        LOGGER.debug("[page] : {}", "사용자 프로필");
        model.addAttribute("user", userRepository.findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다.")));
        return "users/profile";
    }

    @GetMapping("/{id}/form")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) throws IllegalStateException {
        LOGGER.debug("[page] : {}", "사용자 정보 수정 폼");

        if(!HttpSessionUtils.isLoginUser(session)) {
            LOGGER.debug("[page] : {}", "비로그인");
            return "redirect:/users/loginForm";
        }

        User user = HttpSessionUtils.getUserFromSession(session);
        if(!user.matchId(id)) {
            LOGGER.debug("[page] : {}", "다른 회원 정보 열람 요청");
            throw new IllegalStateException("허용되지 않은 요청");
        }

        model.addAttribute("user", user);

        return "users/updateForm";
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, User updatedUser, HttpSession session) {
        LOGGER.debug("[page] : {}", "사용자 정보 수정");

        if(!HttpSessionUtils.isLoginUser(session)) {
            LOGGER.debug("[page] : {}", "비로그인");
            return "redirect:/users/loginForm";
        }

        User user = HttpSessionUtils.getUserFromSession(session);
        if(!user.matchId(id)) {
            LOGGER.debug("[page] : {}", "다른 회원 정보 열람 요청");
            throw new IllegalStateException("허용되지 않은 요청");
        }

        if(!user.matchPassword(updatedUser)){
            LOGGER.debug("[page] : {}", "비밀번호 불일치");
            throw new NoSuchElementException("Not Match Password");
        }

        user.update(updatedUser);
        userRepository.save(user);

        return "redirect:/users/list";
    }


}
