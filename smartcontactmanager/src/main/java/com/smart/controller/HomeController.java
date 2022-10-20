package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@RequestMapping("/")
	public String home(Model m)
	{
		m.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model m)
	{
		m.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	@RequestMapping("/signup")
	public String signup(Model m)
	{
		m.addAttribute("title", "Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	@RequestMapping(value = "/do_register", method= RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1, @RequestParam(value = "agreement", defaultValue="false") boolean agreement,Model m, HttpSession session )
	{
		
		try {
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR "+ result1.toString());
				m.addAttribute("user", user);
				return "signup";
			}
			if(!agreement)
			{
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("Agreement "+agreement);
			System.out.println("User "+user);
			
			User result = this.userRepository.save(user);
			
			m.addAttribute("user",new User());
			
			session.setAttribute("message", new Message("Sussessfully Registered","alert-success" ));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong"+e.getMessage(),"alert-danger" ));
			return "signup";
		}
		
	}
	
	
	@RequestMapping("/signin")
	public String customerLogin(Model m)
	{
		m.addAttribute("title","Login Page");
		return "login";
	}
}
