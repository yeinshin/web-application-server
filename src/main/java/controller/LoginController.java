package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

public class LoginController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        // userId로 회원 정보 받아오기
        User user = DataBase.findUserById(request.getParameter("userId"));
        log.debug("userId: {}", request.getParameter("userId"));

        // 아이디와 비밀번호 입력 값이 안들어왔을 때 (null 처리) or 아이디 or 비밀번호가 틀렸을 때
        if(user == null || !user.getPassword().equals(request.getParameter("password"))){
            response.forward("/user/login_failed.html");
            return;
        }

        // 로그인 성공했을 때
        if (user.login(request.getParameter("password"))) {
            HttpSession session = request.getSession();
            session.setAttribute("user",user);
            response.sendRedirect("/index.html");
        }

    }
}
