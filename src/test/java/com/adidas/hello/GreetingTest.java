package com.adidas.hello;

import org.junit.Test;

import static org.junit.Assert.*;

public class GreetingTest {
    @Test
    public void getContent() throws Exception {

        String fixture = "test content";
        Greeting greeting = new Greeting(fixture);
        assertEquals("Greeting content", fixture, greeting.getContent());

    }

}
