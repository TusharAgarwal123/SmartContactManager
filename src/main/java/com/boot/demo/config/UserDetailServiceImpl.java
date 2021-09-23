package com.boot.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.boot.demo.dao.UserRepository;
import com.boot.demo.entity.User;

public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository ur;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// fetching user from database.

		User user = ur.getUserByUserName(username);

		if (user == null) {
			throw new UsernameNotFoundException("Could not found user!!");
		}

		CustomUserDetail cs = new CustomUserDetail(user);
		return cs;

	}

}
