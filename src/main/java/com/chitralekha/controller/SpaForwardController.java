package com.chitralekha.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/",
            "/discover",
            "/movies",
            "/shows",
            "/anime",
            "/books",
            "/lists",
            "/lists/**",
            "/content/**",
            "/messages",
            "/notifications",
            "/profile/**",
            "/privacy",
            "/terms"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
