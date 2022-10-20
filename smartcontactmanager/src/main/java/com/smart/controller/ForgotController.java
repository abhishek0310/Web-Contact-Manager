package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.service.EmailService;

@Controller
public class ForgotController{
	
	@Autowired
	private EmailService emailService;
	
	Random random = new Random(1000);
	
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session)
	{
		System.out.println("Email "+email);
		
		
		
		int otp = random.nextInt(99999999);
		
		System.out.println("OTP "+otp);
		
		//code for sending otp to mail
		
		String subject = "OTP FROM SCM";
		String message = "Hi Abhishek this is you otp for smart contact manager. Its valid for Next 10 mins"
				+ "<h1> OTP = "+otp+" </h1>";
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag)
		{
			return "verify_otp";
			
		}else
		{
			session.setAttribute("message", "Check your email id !!");
			return "forgot_email_form";
		}
		
		
	}
}