package com.cos.costagram.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// start address : /auth
@Controller
public class AuthController {
	@GetMapping("/auth/loginForm")
	public String loginForm() {
		return "auth/loginForm";
	}
	@GetMapping("/auth/joinForm")
	public String joinForm() {
		return "auth/joinForm";
	}
}
