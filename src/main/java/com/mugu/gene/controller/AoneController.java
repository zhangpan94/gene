package com.mugu.gene.controller;

import com.mugu.gene.service.AoneService;
import com.mugu.gene.web.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author : zp
 * @Description :
 * @Date : 2020/4/1
 */
@Controller
public class AoneController {

    @Autowired
    private AoneService aoneService;

    @RequestMapping("/aone")
    public JsonResult aone(Double pval,Integer under,String peopele,String threshold){
        return aoneService.aone(pval,under,peopele,threshold);
    }

}
