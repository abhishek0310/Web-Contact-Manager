package com.smart.controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model m, Principal p) {
		String userName = p.getName();
		User user = userRepository.getUserByName(userName);

		m.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model m, Principal p) {
		m.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal p, HttpSession session) {
		try {

			String name = p.getName();
			User user = this.userRepository.getUserByName(name);

			// processing and uploading file
			if (file.isEmpty()) {
				contact.setImage("contact.png");
				System.out.println("File is Empty");

			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is Uploaded");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("DATA " + contact);

			System.out.println("Added to data base");

			// message success

			session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));

		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();

			// message error
			session.setAttribute("message", new Message("Something went wrong !! Try again..", "danger"));
		}
		return "normal/add_contact_form";
	}

	// show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal p) {
		m.addAttribute("title", "Show User Contacts");

		String userName = p.getName();
		User user = this.userRepository.getUserByName(userName);

		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model m, Principal p) {
		System.out.println("CID " + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String userName = p.getName();
		User user = this.userRepository.getUserByName(userName);

		if (user.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		}

		return "normal/contact_details";
	}

	// delete contact handler
	@RequestMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model m, Principal p, HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String userName = p.getName();
		User user = this.userRepository.getUserByName(userName);

		if (user.getId() == contact.getUser().getId()) {
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact deleted successfully.....", "success"));
		}

		return "redirect:/user/show-contacts/0";
	}

	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cid).get();

		m.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal p) {
		
		try {
			 Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty())
			{
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();
				
				
			    //add new photo
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldContactDetails.getImage());
			}
			User user = this.userRepository.getUserByName(p.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("messages", new Message("Your contact is updated..!", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("CONTACT NAME " + contact.getName());
		System.out.println("CONTACT ID " + contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//update profile
	@PostMapping("/update-profile/{id}")
	public String updateProfile(@PathVariable("id") Integer id, Model m, Principal p) {
		m.addAttribute("title", "Update profile");

		String name = p.getName();
		User userId = this.userRepository.getUserByName(name);


		m.addAttribute("user", userId);
		return "normal/update_profile";
	}
	
	//update profile handler
	@RequestMapping(value = "/profile-update", method = RequestMethod.POST)
	public String updateProfileHandler(@ModelAttribute User user,@RequestParam("UserProfileImage") MultipartFile file,
			Model m, HttpSession session, Principal p)
	{
		try {
			
			String name = p.getName();
			User userId = this.userRepository.getUserByName(name);
			
			if(!file.isEmpty())
			{
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, userId.getImageUrl());
				file1.delete();
				
				
			    //add new photo
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				user.setImageUrl(file.getOriginalFilename());
			}
			else
			{
				user.setImageUrl(user.getImageUrl());
			}
			
			this.userRepository.save(user);

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "normal/profile";
	}
	
	//open settings handler
	
	@GetMapping("/settings")
	public String opensettings()
	{
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword, Principal p, HttpSession session )
	{
		System.out.println("OLD Passowrd"+oldPassword);
		System.out.println("NEW password"+newPassword);
		
		String userName = p.getName();
		User currentUser = this.userRepository.getUserByName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed..", "success"));
			
		}else
		{
			session.setAttribute("message", new Message("Please enter correct old Password..!!", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}

}
