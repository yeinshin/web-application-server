package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
    private String path = "";
    private Map<String,String> parameter = new HashMap<>();
    private HttpMethod method;
    public RequestLine (String requestLine){
        log.debug("requestLine: {}",requestLine);
        String[] tokens = requestLine.split(" ");

        method = HttpMethod.valueOf(tokens[0]);

        if(method.isPost()){
            path = tokens[1];
            log.debug("POST path: {}",path);
            return;
        }

        int idx = tokens[1].indexOf("?");
        log.debug("idx: {}",idx);

        if(idx == -1) {
            path = tokens[1];
            log.debug("GET path: {}",path);
        }
        else {
            path = tokens[1].substring(0,idx);
            parameter = HttpRequestUtils.parseQueryString(tokens[1].substring(idx+1));
        }

    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method.toString();
    }

    public Map<String, String> getParameter() {
        return parameter;
    }
}
