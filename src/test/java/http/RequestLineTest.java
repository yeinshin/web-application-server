package http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestLineTest {

    private RequestLine requestLine;

    @Test
    void create_method() {
        // GET 방식
        requestLine = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals("GET",requestLine.getMethod());
        assertEquals("/index.html",requestLine.getPath());

        // POST 방식
        requestLine = new RequestLine("POST /index.html HTTP/1.1");
        assertEquals("POST",requestLine.getMethod());
        assertEquals("/index.html",requestLine.getPath());

    }

    @Test
    void create_path_and_params() {
        requestLine = new RequestLine("GET /user/create?userId=yein&password=1234 HTTP/1.1");
        assertEquals("GET",requestLine.getMethod());
        assertEquals("/user/create",requestLine.getPath());
        assertEquals(2,requestLine.getParameter().size());
    }

//    @Test
//    void create_method() {
//        // GET 방식
//        RequestLine requestLine = new RequestLine("GET /index.html HTTP/1.1");
//        assertEquals("GET",requestLine.getMethod());
//        assertEquals("/index.html",requestLine.getPath());
//
//        // POST 방식
//        requestLine = new RequestLine("POST /index.html HTTP/1.1");
//        assertEquals("POST",requestLine.getMethod());
//        assertEquals("/index.html",requestLine.getPath());
//
//    }
//
//    @Test
//    void create_path_and_params() {
//        RequestLine requestLine = new RequestLine("POST /user/create?userId=yein&password=1234 HTTP/1.1");
//        assertEquals("GET",requestLine.getMethod());
//        assertEquals("/user/create",requestLine.getPath());
//        assertEquals(2,requestLine.getParameter().size());
//    }



}