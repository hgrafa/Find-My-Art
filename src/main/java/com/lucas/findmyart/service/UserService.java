package com.lucas.findmyart.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.lucas.findmyart.model.enums.Role;
import com.lucas.findmyart.model.user.Authority;
import com.lucas.findmyart.repository.AuthorityRepository;
import com.lucas.findmyart.api.form.UserForm;
import com.lucas.findmyart.service.exceptions.UserAlreadyRegisteredException;
import com.lucas.findmyart.service.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

import com.lucas.findmyart.model.user.User;
import com.lucas.findmyart.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
  private UserRepository userRepository;
  private AuthorityRepository authorityRepository;

  private User toUser(UserForm userForm) {
    User user = new User();
    Role role = null;

    user.setUsername(userForm.getUsername());
    user.setEmail(userForm.getEmail());
    user.setPassword(userForm.getPassword());
    user.setEnabled(true);

    switch (userForm.getRole().toUpperCase()){
      case "MUSICIAN" -> role = Role.MUSICIAN;
      case "PUB" -> role = Role.PUB;
      default -> role = Role.LISTENER;
    }

    Optional<Authority> authority = authorityRepository.findByRole(role);

    if(authority.isEmpty()) {
      Authority authorityEntity = new Authority();
      authorityEntity.setRole(role);
      authorityRepository.save(authorityEntity);
      authority = authorityRepository.findByRole(role);
    }

    user.setAuthority(authority.get());

    return user;
  }

  private boolean  isUserAlreadyRegistered(UserForm userForm) {
    return userRepository.findByUsername(userForm.getUsername()).isPresent();
  }

  public List<User> getAll() {
    return userRepository.findAll();
  }

  public List<User> getUsersByAuthority(String role){
    return userRepository
            .findAll()
            .stream()
            .filter(user -> user.getAuthority().getRole().toString().equalsIgnoreCase(role))
            .collect(Collectors.toList());
  }

  public User getById(Long id) throws UserNotFoundException {
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("user not found by this id!"));
  }

  public User getByUsername(String username) throws UserNotFoundException {

    return userRepository
            .findAll()
            .stream()
            .filter(user -> user.getUsername().equalsIgnoreCase(username))
            .findFirst()
            .orElseThrow(() -> new UserNotFoundException("user not found by this username!"));

//    return userRepository.findByUsername(username)
//            .orElseThrow(() -> new UserNotFoundException("user not found by this username!"));
  }

  public User register(UserForm userForm) throws UserAlreadyRegisteredException {
    if( isUserAlreadyRegistered(userForm) ) {
      String errorMessage = "Username \"" + userForm.getUsername() +"\" already registered!";
      throw new UserAlreadyRegisteredException(errorMessage);
    }

    User user = toUser(userForm);
    return userRepository.save(user);
  }

  private Authority registerAuthority(Role role) {
    // TODO make register repository process
  }

}
