package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by rodrigo on 30/10/16.
 */
@Controller
public class RedirectController {
    @RequestMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}
