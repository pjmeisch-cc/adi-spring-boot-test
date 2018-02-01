package com.adidas.hello;

import com.adidas.hello.security.WithMockOAuth2User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class LoggedInControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    public void shouldReturnFalseWhenUserIsNotLoggedIn() throws Exception {
        mockMvc.perform(get("/logged-in", String.class))
            .andExpect(status().isOk())
            .andExpect(content().string("false"))
            .andReturn();
    }

    @Test
    @WithMockOAuth2User
    public void shouldReturnTrueWhenUserIsLoggedIn() throws Exception {
        mockMvc.perform(get("/logged-in", String.class))
            .andExpect(status().isOk())
            .andExpect(content().string("true"))
            .andReturn();
    }


}
