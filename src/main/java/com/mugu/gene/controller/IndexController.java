package com.mugu.gene.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author : zp
 * @Description :
 * @Date : 2019/11/1
 */
@Controller
@RequestMapping("/")
public class IndexController {
    @RequestMapping("")
    public String index() {
        return "forward:index.html";
    }


}
