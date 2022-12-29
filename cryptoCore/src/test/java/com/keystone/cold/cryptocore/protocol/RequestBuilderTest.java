package com.keystone.cold.cryptocore.protocol;

import junit.framework.TestCase;

import org.junit.Test;

public class RequestBuilderTest extends TestCase {
    @Test
    public void testRequest() {
        SignRequestBuilder rb = new SignRequestBuilder();
        rb.setSignId(0);
        rb.setSignRequest(0, 0, "1", "m/44'/60'/0'/0/0", "hellorust", "/dev/ttyMT1");

        String output = rb.build();
        assertEquals( "122d1a0131220968656c6c6f727573742a106d2f3434272f3630272f30272f302f30320b2f6465762f7474794d5431", output);
    }
}