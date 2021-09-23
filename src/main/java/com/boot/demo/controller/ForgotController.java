package com.boot.demo.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.demo.dao.UserRepository;
import com.boot.demo.entity.User;
import com.boot.demo.service.EmailService;

@Controller
public class ForgotController {

	Random random = new Random(1000); // smallest 4 digit value.

	@Autowired
	private EmailService es;

	@Autowired
	private UserRepository ur;

	@Autowired
	private BCryptPasswordEncoder be;

	// email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		System.out.println(email);

		// generating otp of 4 digit.

		int otp = random.nextInt(99999);
		System.out.println(otp);

		// code for send otp to email.

		String subject = "OTP from Smart Contact Manager";
		String msg = "OTP is: " + otp;
		String to = email;

		boolean flag = es.sendEmail(msg, subject, to);

		if (flag) {
			session.setAttribute("myOtp", otp);
			session.setAttribute("email", email);
			return "verify-otp";
		} else {

			session.setAttribute("message", "Check your email id");
			return "forgot_email_form";
		}

	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myOtp = (int) session.getAttribute("myOtp");
		String email = (String) session.getAttribute("email");
		if (myOtp == otp) {

			User user = this.ur.getUserByUserName(email);

			// if user with this email is not present.
			if (user == null) {
				// send error msg.
				session.setAttribute("message", "User does not exits with this email!!");
				return "forgot_email_form";

			} else {
				// show change password form.
				return "password-change-form";
			}
		} else {
			session.setAttribute("message", "You have entered wrong otp!!");
			return "verify-otp";
		}

	}

	// change password handler.
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("pass") String pass, HttpSession session) {

		String email = (String) session.getAttribute("email");
		User user = this.ur.getUserByUserName(email);
		user.setPassword(this.be.encode(pass));
		this.ur.save(user);
		session.setAttribute("message", "");
		return "redirect:/signin?change=password changed successfully...";
	}

}
