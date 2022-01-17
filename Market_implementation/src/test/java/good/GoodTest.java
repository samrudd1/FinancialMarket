package good;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import session.Session;

public class GoodTest {

    @BeforeAll
    static void setup(){
        Session.openSession();
    }

    @Test
    void createGoodTest() throws InterruptedException {
        Good testGood = new Good(true);
        Session.getGoodsToDelete().add(testGood);
        assertEquals("Good" + testGood.getId(),Good.getName());
    }

    @AfterAll
    static void clearUp(){
        Session.closeSession();
    }

}
