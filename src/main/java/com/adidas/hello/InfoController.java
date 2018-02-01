package com.adidas.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {
    private final InfoEndpoint infoEndpoint;

    public InfoController(@Autowired InfoEndpoint infoEndpoint) {
        this.infoEndpoint = infoEndpoint;
    }
    
    @RequestMapping("/_manage/info")
    public Map<String, Object> info() {
        return infoEndpoint.invoke();
    }


}
