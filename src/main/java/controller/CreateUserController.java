package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 회원 가입 Controller
public class CreateUserController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        User user = new User
                (
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email")
                );

        log.debug("User Test : {}",user);
        DataBase.addUser(user);

        // 302 헤더 : 브라우저는 사용자를 이 URL의 페이지로 리다이렉트 -> responseBody 필요 x
        response.sendRedirect("/index.html");
    }
}
