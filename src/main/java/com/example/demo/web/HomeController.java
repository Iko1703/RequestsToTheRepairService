package com.example.demo.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        boolean dispatcher = hasRole(authentication, "ROLE_DISPATCHER");
        boolean master = hasRole(authentication, "ROLE_MASTER");

        if (dispatcher) {
            return "redirect:/dispatcher";
        } else if (master) {
            return "redirect:/master";
        }

        return "error";
    }

    private boolean hasRole(Authentication auth, String role) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}


