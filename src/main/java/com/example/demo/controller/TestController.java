package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/test")
public class TestController {
    @Value("${app.cors.origins}")
    private String corsValue;

    @GetMapping
    public String test()
    {
        return "Working ! \n CORS URL ENV VARIABLE : " + corsValue;
    }
}
