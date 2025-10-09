package com.zandan.app.filestorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {"/", "/files/**"})
    public String index() {
        return "forward:/index.html";
    }
}

