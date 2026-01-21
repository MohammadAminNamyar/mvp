package com.example.squarespool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/swagger-ui.html")
    public String swaggerRedirect() {
        return "redirect:/tpi/rest/swagger-ui/index.html";
    }
}
