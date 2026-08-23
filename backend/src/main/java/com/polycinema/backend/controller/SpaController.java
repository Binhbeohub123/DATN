package com.polycinema.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    // (?!ws$) — exclude the exact path "ws" so the STOMP WebSocket handshake
    // endpoint is not captured by this SPA catch-all and forwarded to index.html.
    @GetMapping("/{path:(?!ws$)[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
