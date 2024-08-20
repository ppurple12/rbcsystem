package com.rbcsystem.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//simple home controller
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "functional/home"; 
    }
}
