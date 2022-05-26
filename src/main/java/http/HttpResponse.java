package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private DataOutputStream dos = null;
    private Map<String,String> headers = new HashMap<>();

    public HttpResponse(OutputStream out){
        dos = new DataOutputStream(out);
    }

    public void addHeader(String key, String value){
        headers.put(key,value);
    }

    public void forward(String url){
        try {
            byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());

            if(url.endsWith(".css")){
                headers.put("Content-Type","text/css");
            } else if(url.endsWith(".js")){
                headers.put("Content-Type","application/javascript");
            } else{
                headers.put("Content-Type","text/html;charset=utf-8");
            }

            headers.put("Content-Length",body.length+"");
            response200Header();

            // ex) body에 index.html 파일 경로를 읽고 readAllBytes 한 것을 보낸다.
            responsebody(body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    // /user/list
    public void forwardBody(String body){
        byte[] contents = body.getBytes();
        headers.put("Content-Type","text/html;charset=utf-8");
        headers.put("Content-Length",contents.length + "");
        response200Header();
        responsebody(contents);
    }

    public void sendRedirect(String redirectUrl){
        try{
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            processHeaders();
            dos.writeBytes("Location: " + redirectUrl + "\r\n");
            dos.writeBytes("\r\n");

        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void response200Header(){
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            dos.writeBytes("\r\n");

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responsebody(byte[] body){
        try{
            dos.write(body,0,body.length);
            dos.flush();
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void processHeaders(){
        try{
            for(String key : headers.keySet()){
                dos.writeBytes(key+": "+headers.get(key)+" \r\n");
            }
        }catch(IOException e) {
            log.error(e.getMessage());
        }
    }

}
