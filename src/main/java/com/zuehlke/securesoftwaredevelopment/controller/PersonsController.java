package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.PersonRepository;
import com.zuehlke.securesoftwaredevelopment.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class PersonsController {

    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    public PersonsController(PersonRepository personRepository, UserRepository userRepository) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/persons/{id}")
    @PreAuthorize("@securityUtil.canViewPerson(#id)")
    public String person(@PathVariable int id, Model model) {
        return showProfile(id, model);
    }

    @GetMapping("/myprofile")
    @PreAuthorize("hasAuthority('VIEW_MY_PROFILE')")
    public String self(Model model, @AuthenticationPrincipal User user) {
        return showProfile(user.getId(), model);
    }

    @DeleteMapping("/persons/{id}")
    @PreAuthorize("@securityUtil.canUpdatePerson(#id)")
    public ResponseEntity<Void> deletePerson(@PathVariable int id) {
        userRepository.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-person")
    @PreAuthorize("@securityUtil.canUpdatePerson(T(java.lang.Integer).parseInt(#person.id))")
    public String updatePerson(Person person, String username) {
        int id = Integer.parseInt(person.getId());
        personRepository.update(person);
        userRepository.updateUsername(id, username);
        return "redirect:/persons/" + id;
    }

    @GetMapping("/persons")
    @PreAuthorize("hasAuthority('VIEW_PERSONS_LIST')")
    public String persons(Model model) {
        model.addAttribute("persons", personRepository.getAll());
        return "persons";
    }

    @GetMapping(value = "/persons/search", produces = "application/json")
    @PreAuthorize("hasAuthority('VIEW_PERSONS_LIST')")
    @ResponseBody
    public List<Person> searchPersons(@RequestParam String searchTerm) {
        return personRepository.search(searchTerm);
    }

    private String showProfile(int id, Model model) {
        Person person = personRepository.get(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("person", person);
        model.addAttribute("username", userRepository.findUsername(id).orElse(""));
        return "person";
    }
}
