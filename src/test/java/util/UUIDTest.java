package util;

import org.junit.Test;

import java.util.UUID;

public class UUIDTest {
    @Test
    public void uuid(){
        // JDK에서 제공하는 UUID 클래스를 사용해 고유한 아이디를 생성할 수 있다.
        System.out.println(UUID.randomUUID());
    }
}
