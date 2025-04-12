package com.security.controller;


import com.security.config.JsonSanitizer;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    @GetMapping("/version")
    public String welcome(){
        logger.error("Hello world");
        return "Hello world";
    }

    @GetMapping("/validate")
        public String validateInput(@RequestBody JSONObject input) {
        return "Sanitized input: " + input;
    }

    @PostMapping("/sanitize")
    public ResponseEntity<String> sanitizeInput(@RequestBody String rawJson) {
        try {
            String cleanJson = JsonSanitizer.sanitizeJson(rawJson);
            return ResponseEntity.ok(cleanJson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid JSON format or internal error.");
        }
    }
}

