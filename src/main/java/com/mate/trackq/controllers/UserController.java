package com.mate.trackq.controllers;

import com.mate.trackq.exception.EmailExistsException;
import com.mate.trackq.exception.UsernameExistsException;
import com.mate.trackq.model.User;
import com.mate.trackq.service.MailService;
import com.mate.trackq.service.UserService;
import com.mate.trackq.util.DomainUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView loginPage(@RequestParam(value = "error", required = false) String error,
                                  @RequestParam(value = "logout", required = false) String logout) {
        ModelAndView model = new ModelAndView();
        if (error != null) {
            model.addObject("error", "Invalid username or password.");
        }

        if (logout != null) {
            model.addObject("message", "Logged out successfully.");
        }

        model.setViewName("login");
        return model;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public ModelAndView signUpPage() {
        return new ModelAndView("signup", "user", new User());
    }

    @RequestMapping(value = "/signup", method = POST)
    public String signUp(HttpServletRequest request, @ModelAttribute User user) {
        User savedUser = userService.addNewUser(user);

        userService.sendConfirmationEmail(savedUser, DomainUtils.getUrl(request));
        return "redirect:/login";
    }

    //TODO Check username on frontend
    @ExceptionHandler(UsernameExistsException.class)
    public ModelAndView usernameExistsHandler() {
        return new ModelAndView("signup", "error", "Username already exists.");
    }

    //TODO Check email on frontend
    @ExceptionHandler(EmailExistsException.class)
    public ModelAndView emailExistsHandler() {
        return new ModelAndView("signup", "error", "Email already exists.");
    }

    @RequestMapping(method = GET, value = "/forgot-password")
    public String forgotPasswordPage() {
        return "forgotPassword";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/forgot-password")
    public ModelAndView forgotPassword(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user != null) {
            mailService.sendChangePasswordEmail(user.getEmail());
        }
        ModelAndView mv = new ModelAndView("forgotPassword");
        mv.addObject("message", "Change Password confirmation sent on your email.");
        return mv;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/change-password/{secret}")
    public ModelAndView changePasswordPage(@PathVariable String secret, HttpServletResponse response)
            throws IOException {
        User user = userService.retrieveUserFromSecret(secret);
        if (user != null) {
            return new ModelAndView("setNewPassword", "user", user);
        } else {
            response.sendError(404);
            return null;
        }
    }

    @RequestMapping(method = POST, value = "/change-password/{secret}")
    public ModelAndView changePassword(@PathVariable String secret, @RequestParam String newPassword,
                                       HttpServletResponse response) throws IOException {
        User user = userService.retrieveUserFromSecret(secret);
        if (user != null) {
            userService.changePassword(user, newPassword);
            return new ModelAndView("login", "message", "Your password has been change.");
        } else {
            response.sendError(404);
            return null;
        }
    }
}
