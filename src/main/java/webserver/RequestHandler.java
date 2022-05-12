package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

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

            // 요청 헤더 읽기
            while(!"".equals(line)){
                line = br.readLine();
                log.debug("header : {}",line);
            }

            if(url.length()>=12 && url.substring(0,12).equals("/user/create")){
                int idx = url.indexOf("?");
                String queryString = url.substring(idx+1);
                log.debug("queryString Test: {} " , queryString);
                Map<String,String > info = HttpRequestUtils.parseQueryString(queryString);

//                try { System.out.println(URLDecoder.decode(userId, "utf-8")); }catch (Exception e){ System.out.println(e.getMessage()); }
                User user = new User(info.get("userId"),info.get("password"),info.get("name"),info.get("email"));
                // User 객체 Test
                log.debug("User Test : {}",user);
            }
            else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    // 응답 헤더
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            // 상태 라인 -> 200 : 성공을 의미
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

    // 응답 본문
    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
