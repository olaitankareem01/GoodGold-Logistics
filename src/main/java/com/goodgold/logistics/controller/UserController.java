package com.goodgold.logistics.controller;

import com.goodgold.logistics.model.Role;
import com.goodgold.logistics.model.User;
import com.goodgold.logistics.model.viewmodels.RegisterUserModel;
import com.goodgold.logistics.repository.RoleRepository;
import com.goodgold.logistics.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {
    final
    PasswordEncoder passwordEncoder;
    final UserRepository userRepository;
    final
    RoleRepository roleRepository;
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/users/create")
    public String create(Model model){
        return "user/register";
    }

    @PostMapping(value = "/users/register")
    public String register(Model model, RedirectAttributes redirectAttributes, RegisterUserModel registerUserModel){
        //String regex="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\\\S+$).{8,20}$";
        //Pattern p = Pattern.compile(regex);
        // Matcher m = p.matcher(registerUserModel.getPassword());
        if(!registerUserModel.getPassword().equals(registerUserModel.getConfirmPassword())){
            redirectAttributes.addAttribute("passError","Password does not match ");
        }
        else if( userRepository.existsByUsername(registerUserModel.getUserName())){
            redirectAttributes.addAttribute("emailError","this email already exist");
        }
        else if((!registerUserModel.getUserName().contains("@")) || (!registerUserModel.getUserName().contains("."))){
            redirectAttributes.addAttribute("emailError","the email is not a valid email address");
        }
        else if(registerUserModel.getAge()<18){
            redirectAttributes.addAttribute("ageError","You must be 18+ to register");
        }
        else if( registerUserModel.getPassword().isBlank()||registerUserModel.getPassword().isEmpty()){
            redirectAttributes.addAttribute("passError","Password can not be empty or blank ");
        }

        //       else if( !m.matches()){
//            redirectAttributes.addAttribute("error","Password is not strong enough");
//        }
        else{
            User u = new User();
            u.setUsername(registerUserModel.getUserName());
            u.setPassword(passwordEncoder.encode(registerUserModel.getPassword()));
            Optional<Role> optionalRole= roleRepository.findByName("SELLER");
            if(optionalRole.isPresent()) {
                Role role =optionalRole.get();
                List<Role> roleList = new ArrayList<>();
                roleList.add(role);
                u.setRoles(roleList);
            }

            u.setFirstName(registerUserModel.getFirstName());
            u.setLastName(registerUserModel.getLastName());
            u.setAge(registerUserModel.getAge());
            u.setSex(registerUserModel.getSex());
            u.setAddress(registerUserModel.getAddress());
            u.setCity(registerUserModel.getCity());
            u.setState(registerUserModel.getState());
            u.setCountry(registerUserModel.getCountry());
            u.setPhoneNo(registerUserModel.getPhoneNo());
            u.setTitle("Seller");

            userRepository.save(u);
            //redirectAttributes.addAttribute("error","");
            return "redirect:/login";
        }
        return "redirect:/users/create";
    }

    @RequestMapping(value = "/users/list", method = RequestMethod.GET)
    public String users(Model model){
        model.addAttribute("users", userRepository.findAll());
        return "user/list";
    }

    @RequestMapping(value = "/sellers/list", method = RequestMethod.GET)
    public String Sellers(Model model){
        model.addAttribute("sellers", userRepository.findUsersByTitleEquals("Seller"));
        return "user/SellersList";
    }

    @RequestMapping(value = "/staffs/list", method = RequestMethod.GET)
    public String staffs(Model model){
        model.addAttribute("staffs", userRepository.findUsersByTitleEquals("Staff"));
        return "staff/list";
    }

    @GetMapping("/staffs/create")
    public String createStaff(Model model){
        return "staff/create";
    }

    @RequestMapping(value = "/staffs/add", method = RequestMethod.POST)
    public String addStaff(Model model, RedirectAttributes redirectAttributes, RegisterUserModel registerUserModel) {

        if(!registerUserModel.getPassword().equals(registerUserModel.getConfirmPassword())){
            redirectAttributes.addAttribute("passError","Password does not match ");
        }
        else if( userRepository.existsByUsername(registerUserModel.getUserName())){
            redirectAttributes.addAttribute("emailError","this email already exist");
        }
        else if((!registerUserModel.getUserName().contains("@")) || (!registerUserModel.getUserName().contains("."))){
            redirectAttributes.addAttribute("emailError","the email is not a valid email address");
        }
        else if( registerUserModel.getPassword().isBlank()||registerUserModel.getPassword().isEmpty()){
            redirectAttributes.addAttribute("passError","Password can not be empty or blank ");
        }
        else{
            User u = new User();
            u.setUsername(registerUserModel.getUserName());
            u.setPassword(passwordEncoder.encode(registerUserModel.getPassword()));
            Optional<Role> optionalRole= roleRepository.findByName("STAFF");
            if(optionalRole.isPresent()) {
                Role role =optionalRole.get();
                List<Role> roleList = new ArrayList<>();
                roleList.add(role);
                u.setRoles(roleList);
            }

            u.setFirstName(registerUserModel.getFirstName());
            u.setLastName(registerUserModel.getLastName());
            u.setAge(registerUserModel.getAge());
            u.setSex(registerUserModel.getSex());
            u.setAddress(registerUserModel.getAddress());
            u.setCity(registerUserModel.getCity());
            u.setState(registerUserModel.getState());
            u.setCountry(registerUserModel.getCountry());
            u.setPhoneNo(registerUserModel.getPhoneNo());
            u.setJobTitle(registerUserModel.getJobTitle());
            u.setTitle("Staff");

            userRepository.save(u);
            return "redirect:/staffs/list";
        }


        return "redirect:/staffs/create";
    }

    @RequestMapping(value = "/staffs/assignRole/{id}", method = RequestMethod.GET)
    public String showAssignRoleForm(@PathVariable("id") long id, Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("staff", userRepository.findById(id).get());
        return "staff/assignRole";
    }

    @RequestMapping(value = "/staffs/assignNewRole", method = RequestMethod.POST)
    public String updateRole(Model model, @RequestParam long id, @RequestParam String name) {

        User s= userRepository.findById(id).get();
        Role role = roleRepository.findByName(name).get();

        List<Role> roleList = s.getRoles();
        roleList.add(role);
        s.setRoles(roleList);


        userRepository.save(s);

        return "redirect:/staffs/list";
    }


    @RequestMapping(value = "/staffs/delete/{id}", method = RequestMethod.GET)
    public String remove(@PathVariable("id") long id, Model model) {

        User s = userRepository.findById(id).get();

        userRepository.delete(s);
        return "redirect:/staffs/list";
    }

}
