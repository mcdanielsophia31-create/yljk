package com.example.keshe1.controller;

import com.example.keshe1.entity.Hospital;
import com.example.keshe1.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping("/")
    public String home(Model model) {
        // 获取所有医院列表，用于前端下拉选择
        List<Hospital> hospitals = hospitalService.list();
        model.addAttribute("hospitals", hospitals);
        return "login"; // 返回登录页面
    }

    @GetMapping("/login")
    public String login(Model model) {
        // 获取所有医院列表，用于前端下拉选择
        List<Hospital> hospitals = hospitalService.list();
        model.addAttribute("hospitals", hospitals);
        return "login"; // 返回登录页面
    }
}