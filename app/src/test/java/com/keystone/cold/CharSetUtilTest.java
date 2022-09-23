package com.keystone.cold;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.keystone.cold.util.CharSetUtil;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class CharSetUtilTest {
    @Test
    public void testIsUTF8Format() {
        String str = "这是一条测试数据  this is a test data  이것은 테스트 데이터이다";
        assertTrue(CharSetUtil.isUTF8Format(str.getBytes(StandardCharsets.UTF_8)));

        String notAStr = "879a053d4800c6354e76c7985a865d2922c82fb5b3f4577b2fe08b998954f2e0";
        assertFalse(CharSetUtil.isUTF8Format(Hex.decode(notAStr)));
    }
}
