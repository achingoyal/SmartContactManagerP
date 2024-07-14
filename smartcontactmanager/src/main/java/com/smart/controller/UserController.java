package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repositories.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		
		User user = userRepository.getUSerByUserName(userName);
		
		model.addAttribute("user", user);
		
		
	}

	@RequestMapping("/index")
	public String dashboard( Model model, Principal principal) {
		
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	//open add handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	//add contact data to db
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		
		try {
			String name = principal.getName();
			User user = this.userRepository.getUSerByUserName(name);
		
			//processing and uploading file
			
			if(file.isEmpty()) {
				//show msg to console
			}
			else {
				//upload file to folder and add name in db 
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image uploaded");
			}
			contact.setUser(user);
			user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("added to database");
		
		//message success
		session.setAttribute("message", new Message("Contact has been added Successfully, Add More!!", "success"));
		} catch (Exception e) {
			
			System.out.println("error "+ e.getMessage());
			e.printStackTrace();
			//Message Error
			session.setAttribute("message", new Message("Smething went wrong, Try Again!!", "danger"));
			
		}
		
		return "normal/add_contact_form";
	}
}
