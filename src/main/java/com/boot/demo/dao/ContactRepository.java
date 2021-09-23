package com.boot.demo.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.boot.demo.entity.Contact;
import com.boot.demo.entity.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// pagination

	// this method will return the contact list of login user.
//	@Query("from Contact as c where c.user.id =:userId")
//	public List<Contact> findContactsByUser(@Param("userId") int userId);

	// page is sublist of a list object. it allows gain information about the
	// position of entire list.
	@Query("from Contact as c where c.user.id =:userId")
	// currentPage =>page
	// contact per page =>5
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);

	// find all contact that will contain name in their name.
	// we are passing user also because we want to find the contact of login user
	// only.
	public List<Contact> findByNameContainingAndUser(String name, User user);

}
