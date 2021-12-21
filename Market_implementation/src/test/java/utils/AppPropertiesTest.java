package utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AppPropertiesTest {

    @Test
    void readJsonTest() {
        String fileDump = AppProperties.getFileDump();
        assertNotNull(fileDump);
        assertNotEquals("{}",fileDump);
    }

    @Test
    void retrieveDataTest(){
        String testData = AppProperties.getProperty("testString");
        assertEquals("test",testData);
    }
}
