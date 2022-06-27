package http;

import java.util.HashMap;
import java.util.Map;

// HttpSessions : 모든 클라이언트의 세션을 관리할 수 있는 저장소
public class HttpSessions {
    public static Map<String,HttpSession> sessions = new HashMap<>();

    public static HttpSession getSession(String id){
        HttpSession session = sessions.get(id);

        if(session==null){
            session = new HttpSession(id);
            sessions.put(id, session);
            return session;
        }
        return session;
    }

    static void remove(String id){
        sessions.remove(id);
    }

}
