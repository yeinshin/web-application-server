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

        String[] tokens = line.split(" ");
        // GET or POST
        method = tokens[0];
        
        // 본문 데이터에 대한 길이 (POST 방식에서 body의 길이)
        int contentLength = 0;

        boolean isLogin = false;
        while(!"".equals(line = br.readLine())){
            log.debug("header : {}",line);

            HttpRequestUtils.Pair header= HttpRequestUtils.parseHeader(line);
            headerMap.put(header.getKey(),header.getValue());

            if ("Content-Length".equals(header.getKey())){
                contentLength = Integer.parseInt(header.getValue());
            }
        }

        // GET 방식일 때
        if("GET".equals(method)){
            int idx = tokens[1].indexOf("?");
            // ex : /user/create
            path = tokens[1].substring(0,idx);

            // QueryString Map 형식으로 저장 (URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임)
            queryString = HttpRequestUtils.parseQueryString(tokens[1].substring(idx+1));
        }
        // POST 방식일 때
        else{
            path = tokens[1];
            String body = IOUtils.readData(br,contentLength);
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
}
