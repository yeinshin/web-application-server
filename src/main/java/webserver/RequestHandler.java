package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        // 소켓이 하나 열리고 클라이언트가 데이터를 전송하는 걸 InputStream으로 받아옴
        // 서버가 클라이언트한테 데이터를 전송하는데 OutputStream 이용
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String path = getDefaultPath(request.getPath());
            // 회원 가입
            if("/user/create".equals(path)){
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
            // 로그인
            else if ("/user/login".equals(path)){
                // userId로 회원 정보 받아오기
                User user = DataBase.findUserById(request.getParameter("userId"));
                log.debug("userId: {}", request.getParameter("userId"));

                // 아이디와 비밀번호 입력 값이 안들어왔을 때 (null 처리) or 아이디 or 비밀번호가 틀렸을 때
                if(user == null || !user.getPassword().equals(request.getParameter("password"))){
                    response.forward("/user/login_failed.html");
                    return;
                }
                // 로그인 성공했을 때
                if (user.getPassword().equals(request.getParameter("password"))) {
                    response.addHeader("Set-Cookie","logined=true");
                    response.sendRedirect("/index.html");
                }
            }
            // 사용자 목록 출력
            else if ("/user/list".equals(path)){

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
            else{
                response.forward(path);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private String getDefaultPath(String path){
        if(path.equals("/")) return "/index.html";
        return path;
    }

}
