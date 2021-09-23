package com.boot.demo.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.demo.dao.UserRepository;
import com.boot.demo.entity.User;
import com.boot.demo.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder be;

	@Autowired
	private UserRepository ur;

	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home- Smart Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About- Smart Contact Manager");
		return "about";
	}

	@GetMapping("/signUp")
	public String signUp(Model m) {
		m.addAttribute("title", "SignUp- Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signUp";
	}

	// this is for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User u, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");

				throw new Exception("You have not agreed the terms and conditions");
			}

			if (result1.hasErrors()) {
				System.out.println("ERROR!! " + result1.toString());
				m.addAttribute("user", u);
				return "signUp";
			}

			u.setRole("ROLE_USER");
			u.setEnabled(true);
			u.setPassword(be.encode(u.getPassword()));

			System.out.println(agreement);
			System.out.println(u);

			User result = this.ur.save(u);
			m.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered!!", "alert-success"));
			return "signUp";
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", u);
			session.setAttribute("message", new Message("Something went wrong!!" + e.getMessage(), "alert-danger"));
			return "signUp";
		}

	}

	@GetMapping("/signin")
	public String customLogin(Model m) {
		m.addAttribute("title", "Login Page");
		return "login";
	}

	@GetMapping("/error")
	public String error() {
		return "error";
	}

}
