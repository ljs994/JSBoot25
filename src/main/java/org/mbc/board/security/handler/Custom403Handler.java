package org.mbc.board.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Log4j2  // Custom403Handler는 CustomSecurityConfig 클래스에서 활용됨!!
public class Custom403Handler implements AccessDeniedHandler {
    //                                   AccessDeniedHandler 내장된 예외처리용 클래스 (재정의)
    // 현재 사용자의 권한이 없거나
    // 특정 조건이 맞지않아 예외발생하는 403 처리용 클래스
    // form 태그를 통해서 전송된 결과를 처리하거나
    // Axios를 이용해서 비동기 처리 Ajax을 이용함으로 두가지 경우 다른 메세지 처리함

    // <form> 태그의 요청이 403인 경우 로그인 페이지로 이동할 때 ACCESS_DENIED
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        log.info("Custom403Handler.handle메서드 실행.....");
        log.info("현재 사용자의 권한이 없거나 조건이 맞지 않습니다.!!!!");
        log.info("403예외발생중.!!!!!!!");

        response.setStatus(HttpStatus.FORBIDDEN.value()); // 현재 예외상태값 저장
        String contentType = request.getHeader("Content-Type"); // json 여부
        boolean jsonRequest = contentType.startsWith("application/json");
        log.info("isJSON : " + jsonRequest);  // json일때 처리 끝

        // 일반요청시
        if(!jsonRequest) {
            response.sendRedirect("/member/login?error=ACCESS_DENIED");
        } // 403예외발생시 로그인 화면으로 이동하고 error 처리

    }




    // Ajax 비동기 처리 JSON 처리


}
