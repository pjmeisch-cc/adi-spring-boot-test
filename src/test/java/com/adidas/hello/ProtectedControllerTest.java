package com.adidas.hello;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtectedControllerTest {
    @Test
    public void protectedTest() throws Exception {
        ProtectedController protectedController = new ProtectedController();
        String response = protectedController.getProtected();
        assertEquals("ProtectedController returns 'protected'",
            "protected",
            response);
    }
}
