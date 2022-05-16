package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import webserver.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private String path = null, method = null;
    private Map<String,String> queryString = new HashMap<>();
    private Map<String,String> headerMap = new HashMap<>();
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        String line = br.readLine();

        if(line==null) return;

        processRequestLine(line);

        boolean isLogin = false;
        while(!"".equals(line = br.readLine())){
            log.debug("header : {}",line);
            HttpRequestUtils.Pair header= HttpRequestUtils.parseHeader(line);
            headerMap.put(header.getKey(),header.getValue());
        }

        // POST 방식일 때의 body parsing
        if("POST".equals(method)){
            String body = IOUtils.readData(br,Integer.parseInt(headerMap.get("Content-Length")));
            queryString = HttpRequestUtils.parseQueryString(body);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String header) {
        return headerMap.get(header);
    }

    public String getParameter(String parameter) {
        return queryString.get(parameter);
    }

    private void processRequestLine(String requestLine){
        String[] tokens = requestLine.split(" ");
        // GET or POST
        method = tokens[0];

        // GET 방식일 때
        if("GET".equals(method)){
            int idx = tokens[1].indexOf("?");

            // ex : /user/create
            if(idx == -1) path = tokens[1];
            else path = tokens[1].substring(0,idx);

            // QueryString Map 형식으로 저장 (URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임)
            queryString = HttpRequestUtils.parseQueryString(tokens[1].substring(idx+1));
        }
        // POST 방식일 때
        else{
            path = tokens[1];
        }
    }
}
