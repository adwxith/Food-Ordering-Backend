package com.foodordering.controller.web;

import com.foodordering.service.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MenuService menuService;

    public HomeController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("menuItems", menuService.getAvailableMenuItems());
        return "home";
    }
}