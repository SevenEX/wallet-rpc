import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

public class SimpleTest {
    @Test
    public void testInteger(){
        Long propertyid  = new Long("2147484983");
        System.out.println(propertyid);
        System.out.println(String.format("%016x", propertyid).substring(8));
    }

}
