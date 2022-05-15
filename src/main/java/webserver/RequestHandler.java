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
            BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            String line = br.readLine();
            log.debug("request line: {}",line);

            if(line == null ) return;

            String[] tokens = line.split(" ");
            // token 값 log 찍기
            log.debug("tokens test : {} ",Arrays.toString(tokens));

            String url = tokens[1];
            log.debug("url : {}",url);

            int contentLength = 0;
            Map<String,String> headerMap = new HashMap<>();

            // 로그인 상태를 저장하는 boolean type 변수
            boolean isLogin = false;

            // 요청 헤더 읽기
            while(!"".equals(line = br.readLine())){
                log.debug("header : {}",line);

                String[] requestHeader = line.split(":");
                headerMap.put(requestHeader[0],requestHeader[1]);

                if ("Content-Length".equals(requestHeader[0])){
                    contentLength = Integer.parseInt(headerMap.get(requestHeader[0]).trim());
                }
                else if ("Cookie".equals(requestHeader[0])){
                    String[] cookie = headerMap.get(requestHeader[0]).trim().split("=");
                    log.debug("cookie Test : {}",Arrays.toString(cookie));
                    isLogin = Boolean.parseBoolean(cookie[1]);
                }
            }

            // 사용자 목록 출력
            if("/user/list".equals(url)){
                // 로그인이 안되어 있을 때 -> login.html
                if(!isLogin){
                    responseResource(out,"/user/login.html");
                    return;
                }
                
                // 로그인이 되어있다면 사용자들의 목록을 보여줄 table 생성
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();

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
            // 로그인 기능
            else if("/user/login".equals(url)){
                String requestBody = IOUtils.readData(br,contentLength);
                Map<String, String> info = HttpRequestUtils.parseQueryString(requestBody);
                User user = DataBase.findUserById(info.get("userId"));

                // 아이디와 비밀번호 입력 값이 안들어왔을 때 (null 처리) or 아이디 or 비밀번호가 틀렸을 때
                if(user == null || !user.getPassword().equals(info.get("password"))){
                    responseResource(out,"/user/login_failed.html");
                    return;
                }
                // 로그인 성공했을 때
                if (user.getPassword().equals(info.get("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302HeaderLoginSuccess(dos, "/index.html");
                }

            }
            // POST 방식
            else if("/user/create".equals(url)){
                String requestBody = IOUtils.readData(br,contentLength);
                Map<String,String > info = HttpRequestUtils.parseQueryString(requestBody);

                User user = new User(info.get("userId"),info.get("password"),info.get("name"),info.get("email"));
                // User 객체 Test
                log.debug("User Test : {}",user);
                DataBase.addUser(user);

                // 302 헤더 : 브라우저는 사용자를 이 URL의 페이지로 리다이렉트 -> responseBody 필요 x
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }
            // CSS 지원하기
//            else if(url.endsWith(".css")){
//                DataOutputStream dos = new DataOutputStream(out);
//                byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());
//                response200CssHeader(dos, body.length);
//                responseBody(dos, body);
//            }
            else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());
                // CSS 지원하기
                if(url.endsWith(".css")){
                    response200CssHeader(dos, body.length);
                }
                else{
                    response200Header(dos, body.length);
                }
                responseBody(dos, body);
            }


            // GET 방식
//            if(url.length()>=12 && url.substring(0,12).equals("/user/create")){
//                int idx = url.indexOf("?");
//                String queryString = url.substring(idx+1);
//                log.debug("queryString Test: {} " , queryString);
//
//                Map<String,String > info = HttpRequestUtils.parseQueryString(queryString);
//
//                User user = new User(info.get("userId"),info.get("password"),info.get("name"),info.get("email"));
//                // User 객체 Test
//                log.debug("User Test : {}",user);
//                DataBase.addUser(user);
//            }
//            else {
//                DataOutputStream dos = new DataOutputStream(out);
//                byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());
//                response200Header(dos, body.length);
//                responseBody(dos, body);
//            }

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
}
