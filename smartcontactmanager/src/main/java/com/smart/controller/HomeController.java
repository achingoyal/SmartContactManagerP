package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repositories.UserRepository;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository repo;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manage");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "SignUp - Smart Contact Manage");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value="agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {
	
		
		try {
			System.out.println(user);
			System.out.println("agreement" + agreement);
			
			if(!agreement) {
				
			System.out.println("you have not accepted terma and conditions");
			throw new Exception("you have not accepted terma and conditions");
				
			}
			if(result1.hasErrors()) {
				
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User result = repo.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message",new Message("Successfully Registered!","alert-success") );
			return "signup";
		} catch (Exception e) {
			
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",new Message("Something went Wrong!!" + e.getMessage(),"alert-danger") );
			return "signup";
		}
		
	
	}


	@RequestMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "signin - Smart Contact Manage");
		return "login";
	}
}
