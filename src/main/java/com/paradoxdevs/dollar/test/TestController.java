package com.paradoxdevs.dollar.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String healthCheck() {
        return "Stretch your dollar now!";
    }
}
