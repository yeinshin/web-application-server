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
    private Map<String,String> parameter = new HashMap<>();
    private Map<String,String> headerMap = new HashMap<>();

    private RequestLine requestLine;
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        String line = br.readLine();

        if(line==null) return;

        requestLine = new RequestLine(line);


        while(!"".equals(line = br.readLine())){
            log.debug("header : {}",line);
            HttpRequestUtils.Pair header= HttpRequestUtils.parseHeader(line);
            headerMap.put(header.getKey(),header.getValue());
        }

        log.debug("path: {}",requestLine.getPath());

        // POST 방식일 때의 body parsing
        if("POST".equals(requestLine.getMethod())){
            String body = IOUtils.readData(br,Integer.parseInt(headerMap.get("Content-Length")));
            parameter = HttpRequestUtils.parseQueryString(body);
        }
        else {
            parameter = requestLine.getParameter();
        }
    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String header) {
        return headerMap.get(header);
    }

    public String getParameter(String param) {
        return parameter.get(param);
    }
}
