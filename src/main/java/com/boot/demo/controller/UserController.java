package com.boot.demo.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.boot.demo.dao.ContactRepository;
import com.boot.demo.dao.UserRepository;
import com.boot.demo.entity.Contact;
import com.boot.demo.entity.User;
import com.boot.demo.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder be;

	@Autowired
	private UserRepository ur;

	@Autowired
	private ContactRepository cr;

	@ModelAttribute // by applying this annotation this method will execute for every method of this
					// class.
	public void addCommonData(Model m, Principal p) {
//		// Principle is class in spring security which is use to get the details of
//		// login user.
//
//		// this p.getName() will return the unique identifier of login user,
//		// in our case unique identifier is email id.
		String name = p.getName();

		User user = ur.getUserByUserName(name);
		m.addAttribute("user", user);
	}

	// dashboard home
	@GetMapping("/index")
	public String dashboard(Model m) {

//		// Principle is class in spring security which is use to get the details of
//		// login user.
//
//		// this p.getName() will return the unique identifier of login user,
//		// in our case unique identifier is email id.
//		String name = p.getName();
//
//		User user = ur.getUserByUserName(name);
//		m.addAttribute("user", user);
		m.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	// open add contact handler.
	@GetMapping("/add-contact")
	public String openContactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add-contact-form";
	}

	// update contact
	@GetMapping("/update-contact/{cid}")
	public String updateContactForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");

		Contact cc = cr.findById(cid).get();
		m.addAttribute("contact", cc);

		return "normal/update-contact-form";
	}

	// update user
	@GetMapping("/update-user/{id}")
	public String updateUserForm(@PathVariable("id") Integer id, Model m) {
		m.addAttribute("title", "Update Profile");

		User user = this.ur.findById(id).get();
		m.addAttribute("user", user);

		return "normal/update-user-form";
	}

	// processing the contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("url") MultipartFile file, Principal p,
			HttpSession session) {

		try {
			String name = p.getName();
			User user = ur.getUserByUserName(name);

			// processing and uploading file

			if (file.isEmpty()) {

				System.out.println("Image is empty");

				// if file is not selected then adding the default the file.
				contact.setImageUrl("contact.png");

			} else {
				contact.setImageUrl(file.getOriginalFilename()); // setting the file url

				File f = new ClassPathResource("static/image").getFile();

				// files will be stored in the static/image folder.
				// if you want to see the stored files than right click on project
				// show in=> system explorer=>open project=>target=>classes=>then open image
				// folder.

				Path path = Paths.get(f.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");

			}

			contact.setUser(user);

			user.getContacts().add(contact);

			ur.save(user);

			System.out.println(contact);

			// message success
			session.setAttribute("message", new Message("Your Contact is added !! Add more..", "success"));

		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
			e.printStackTrace();

			// message error.
			session.setAttribute("message", new Message("Something went wrong!! try again", "danger"));
		}

		return "normal/add-contact-form";
	}

	// show contact handler
	// per page =5[n]
	// current page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal p) {
		m.addAttribute("title", "Show User Contact");

		String name = p.getName();
		User user = ur.getUserByUserName(name);

		// currentPage =>page
		// contact per page =>5
		Pageable pg = PageRequest.of(page, 5); // 5 means 5 contact per page

		Page<Contact> list = this.cr.findContactsByUser(user.getId(), pg);
		m.addAttribute("contacts", list);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", list.getTotalPages());

		return "normal/show-contacts";
	}

	// showing particular contact details
	@GetMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model m, Principal p) {

		System.out.println(cid);
		Optional<Contact> cc = this.cr.findById(cid);

		Contact contact = cc.get();

		String name = p.getName();
		User logInUser = ur.getUserByUserName(name);

		// this if condition is making sure that login user can only view the contact
		// details of his contacts.
		if (logInUser.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		}

		return "normal/contact_details";
	}

	// delete contact handler.
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model m, Principal p, HttpSession session) {

		Optional<Contact> cc = this.cr.findById(cid);
		Contact contact = cc.get();

		String name = p.getName();
		User logInUser = ur.getUserByUserName(name);

		if (logInUser.getId() == contact.getUser().getId()) {

			contact.setUser(null);

			// when delete contact ,we should also delete image store in image folder.

			this.cr.delete(contact);
			session.setAttribute("message", new Message("Contact deleted Succesfully", "success"));
		} else {
			session.setAttribute("message", new Message("You can't delete this contact", "danger"));
		}

		return "redirect:/user/show-contacts/0";
	}

	@PostMapping("/update-contact")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("url") MultipartFile file, Principal p,
			HttpSession session) {

		try {

			// fetch old contact details.
			Contact oldContactDetail = this.cr.findById(contact.getCid()).get();

			// if file is selected
			if (!file.isEmpty()) {

				// delete old photo

				File deleteFile = new ClassPathResource("static/image").getFile();
				File f1 = new File(deleteFile, oldContactDetail.getImageUrl());
				f1.delete();

				// update new photo.
				File f = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(f.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImageUrl(file.getOriginalFilename());

			} else {
				// if file is empty, then set the old image url.
				contact.setImageUrl(oldContactDetail.getImageUrl());
			}

			// we have to set the user in the contact.
			User user = ur.getUserByUserName(p.getName());
			contact.setUser(user);

			this.cr.save(contact);

			session.setAttribute("message", new Message("Your contact is updated", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/" + contact.getCid() + "/contact";

	}

	// handler for update user
	@PostMapping("/update-user")
	public String updateUser(@ModelAttribute("user") User user, @RequestParam("url") MultipartFile file, Principal p,
			HttpSession session) {

		try {

			// fetch old user details.
			User oldUserDetails = this.ur.findById(user.getId()).get();

			// if file is selected
			if (!file.isEmpty()) {

				// delete old photo

				File deleteFile = new ClassPathResource("static/image").getFile();
				File f1 = new File(deleteFile, oldUserDetails.getImageUrl());
				f1.delete();

				// update new photo.
				File f = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(f.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				user.setImageUrl(file.getOriginalFilename());

			} else {
				// if file is empty, then set the old image url.
				user.setImageUrl(oldUserDetails.getImageUrl());
			}

			System.out.println(user);

			// set the remaining user properties.
//			user.setRole("ROLE_USER");
//			user.setEnabled(true);
//			user.setPassword(oldUserDetails.getPassword());
//			user.setContacts(oldUserDetails.getContacts());

			this.ur.save(user);

			session.setAttribute("message", new Message("Your Profile is updated", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/profile";

	}

	// show profile handler
	@GetMapping("/profile")
	public String showProfile(Model m) {
		m.addAttribute("title", "Your Profile");
		return "normal/profile";
	}

	// open setting handler

	@GetMapping("/settings")
	public String setting() {
		return "normal/settings";
	}

	// change password handler.
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal p, HttpSession session) {

		User user = ur.getUserByUserName(p.getName());

		// as we have store password in encoded form.
		// so we have to use the matches() method of BCryptPasswordEncoder ,it will take
		// old password
		// entered by the user and encoded store password and compare both.
		if (this.be.matches(oldPassword, user.getPassword())) {
			// change the password.

			// we are updating the new password in encoded form.
			user.setPassword(this.be.encode(newPassword));
			this.ur.save(user);
			session.setAttribute("message", new Message("Your Password is Successfully Changed...", "success"));

		} else {
			// error
			session.setAttribute("message", new Message("Wrong Old Password", "danger"));
			return "redirect:/user/settings";
		}

		return "redirect:/user/index";
	}

}
