package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import http.HttpRequest;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            HttpRequest request = new HttpRequest(in);
            log.debug("request.getPath(): {}",request.getPath());
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
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }
            // 로그인
            else if ("/user/login".equals(path)){
                // userId로 회원 정보 받아오기
                User user = DataBase.findUserById(request.getParameter("userId"));
                log.debug("userId: {}", request.getParameter("userId"));

                // 아이디와 비밀번호 입력 값이 안들어왔을 때 (null 처리) or 아이디 or 비밀번호가 틀렸을 때
                if(user == null || !user.getPassword().equals(request.getParameter("password"))){
                    responseResource(out,"/user/login_failed.html");
                    return;
                }
                // 로그인 성공했을 때
                if (user.getPassword().equals(request.getParameter("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302HeaderLoginSuccess(dos, "/index.html");
                }
            }
            // 사용자 목록 출력
            else if ("/user/list".equals(path)){

                // 로그인이 안되어 있을 때 -> login.html
                if(!isLogin(request.getHeader("Cookie"))){
                    responseResource(out,"/user/login.html");
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

                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = sb.toString().getBytes();
                response200Header(dos,body.length);
                responseBody(dos,body);

            }
            else{
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp"+ path).toPath());
                // CSS 지원하기
                if(path.endsWith(".css")){
                    response200CssHeader(dos, body.length);
                }
                else{
                    response200Header(dos, body.length);
                }
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 응답 헤더 200
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            // 상태 라인 -> 200 : 성공을 의미, OK : 응답구문
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            // 응답 헤더
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            // 헤더와 본문 사이의 빈 공백 라인
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            // 상태 라인 -> 200 : 성공을 의미, OK : 응답구문
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            // 응답 헤더
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            // 헤더와 본문 사이의 빈 공백 라인
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 응답 헤더 302
    private void response302Header(DataOutputStream dos, String url){
        try {
            // 상태 라인 -> 302 : Redirect 의미
            dos.writeBytes("HTTP/1.1 302 redirect \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            // 헤더와 본문 사이의 빈 공백 라인
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 응답 헤더 302 - 로그인 성공했을 때 Set-Cookie
    private void response302HeaderLoginSuccess(DataOutputStream dos, String url){
        try {
            // 상태 라인 -> 302 : Redirect 의미
            dos.writeBytes("HTTP/1.1 302 redirect \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            // 헤더와 본문 사이의 빈 공백 라인
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 응답 본문
    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
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
