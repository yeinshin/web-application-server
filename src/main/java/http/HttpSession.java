package http;

import java.util.HashMap;
import java.util.Map;

// HttpSession : 클라이언트별 세션 저장소 추가
public class HttpSession {
    private Map<String,Object> values = new HashMap<>();

    private String id;

    // HttpSession 생성자
    public HttpSession(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String name, Object value){
        values.put(name,value);
    }

    public Object getAttribute(String name){
        return values.get(name);
    }

    public void removeAttribute(String name){
        values.remove(name);
    }

    public void invalidate(String i) {
        HttpSessions.remove(id);
    }
    
}
