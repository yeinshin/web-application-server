package http;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class httpRequestTest {
    private String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
        HttpRequest request = new HttpRequest(in);

        if(request!=null){
            assertEquals("GET", request.getMethod());
            assertEquals("/user/create",request.getPath());
            assertEquals("keep-alive",request.getHeader("Connection"));
            assertEquals("yein",request.getParameter("userId"));
        }

    }

    @Test
    public void request_POST() throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        HttpRequest request = new HttpRequest(in);

        if(request!=null){
            assertEquals("POST",request.getMethod());
            assertEquals("/user/create",request.getPath());
            assertEquals("keep-alive",request.getHeader("Connection"));
            assertEquals("yein",request.getParameter("userId"));
        }

    }
}
