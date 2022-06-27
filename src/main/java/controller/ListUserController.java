package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import model.User;
import util.HttpRequestUtils;

import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController{
    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        // 로그인이 안되어 있을 때 -> login.html
        if(!isLogin(request.getSession())){
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

    private boolean isLogin(HttpSession session){
        Object user = session.getAttribute("user");

        if(user == null){
            return false;
        }
        return true;
    }
}
