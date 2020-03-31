import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ztuo.bc.wallet.entity.TransactionDto;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

public class SimpleTest {
    @Test
    public void testBigdecimal(){
        BigDecimal a  = new BigDecimal("12.5");
        System.out.println(a);
        a.subtract(BigDecimal.ONE);
        System.out.println(a);
    }
    @Test
    public void test1(){
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setFromAddress(Arrays.asList("2323","2323"));
        HashMap<String, BigDecimal> stringBigDecimalHashMap = new HashMap<>();
        stringBigDecimalHashMap.put("asdf ",BigDecimal.ZERO);
        stringBigDecimalHashMap.put("2asdf323 ",BigDecimal.ONE);
        transactionDto.setTargetAddressesMap(stringBigDecimalHashMap);
        System.out.println(JSON.toJSONString(transactionDto));
    }
}
