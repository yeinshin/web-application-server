package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import util.HttpRequestUtils;

import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController{
    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        // 로그인이 안되어 있을 때 -> login.html
        if(!isLogin(request.getHeader("Cookie"))){
            response.forward("/user/login.html");
            return;
        }

        // 로그인이 되어있다면 사용자들의 목록을 보여줄 table 생성
        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();

        sb.append("<link rel=\"shortcut icon\" href=\"#\">");
        sb.append("<table border='1'>");
        for (User user : users){
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        response.forwardBody(sb.toString());
    }

    // 쿠키를 통해 로그인 상태 확인
    private boolean isLogin(String cookieValue){
        Map<String,String> cookies = HttpRequestUtils.parseCookies(cookieValue);
        String value = cookies.get("logined");

        // value가 null -> Set-Cookie가 안됨 -> login 상태가 x
        if(value==null) return false;
        // login 상태라면 true 반환
        return Boolean.parseBoolean(value);
    }
}
