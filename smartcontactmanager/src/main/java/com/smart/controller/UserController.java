package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;
import com.smart.repositories.ContactRepository;
import com.smart.repositories.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
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
			contact.setImage("default_image.jpg");
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
			session.setAttribute("message", new Message("Something went wrong, Try Again!!", "danger"));
			
		}
		
		return "normal/add_contact_form";
	}
	
	//pagination per page 5, current page -0
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") int page,  Model m, Principal principal) {
		m.addAttribute("title", "show user contact");
		//contact list
		String userName = principal.getName();
		User user = userRepository.getUSerByUserName(userName);
		int id = user.getId();
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = contactRepository.findContactsByUser(id,pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") int cId, Model model, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName = principal.getName();
		User user = userRepository.getUSerByUserName(userName);
		if(user.getId()== contact.getUser().getId()) {
			model.addAttribute("title", contact.getName());
			model.addAttribute("contact", contact);
		}
		else {
			model.addAttribute("title","invalid Contact");
		}
		
		
		
		return "normal/contact_details";
	}

//deleting contact
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") int cId, Principal principal, HttpSession session) throws IOException {
	
		String userName = principal.getName();
		User user = userRepository.getUSerByUserName(userName);
		
		Contact contact = contactRepository.findById(cId).get();
		
		if(user.getId()==contact.getUser().getId()) {
			contact.setUser(null);
			File deleteFile = new ClassPathResource("static/img").getFile();
			File file1= new File(deleteFile, contact.getImage());
			file1.delete();
			contactRepository.deleteById(cId);
		}
		
		
		
		session.setAttribute("message", new Message("Contact deleted Successfully", "success"));
		
		return "redirect:/user/show-contacts/0";
		
	}
	//open update contact form
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cId, Model model) {
		model.addAttribute("title", "update Contact");
		Contact contact = contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update handler
	@RequestMapping(value="/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, 
			Model model, HttpSession session, Principal principal) {
		model.addAttribute("title","updated Form");
		try {
			Contact oldContact = contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1= new File(deleteFile, oldContact.getImage());
				file1.delete();
				//update new photo
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContact.getImage());
			}
			User user = userRepository.getUSerByUserName(principal.getName());
			
			contact.setUser(user);
			
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Contact details has been updated", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
		
	}
//Your Profile Handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Your Profile");
		return "normal/profile";
		
	}
}





