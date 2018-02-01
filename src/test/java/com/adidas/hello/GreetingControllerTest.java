package com.adidas.hello;

import org.junit.Test;

import static org.junit.Assert.*;

public class GreetingControllerTest {
    @Test
    public void greeting() throws Exception {

        GreetingController greetingController = new GreetingController();
        Greeting greeting = greetingController.greeting("John");
        assertEquals("GreetingController greets",
            "Hello, John!",
            greeting.getContent());

    }
}
