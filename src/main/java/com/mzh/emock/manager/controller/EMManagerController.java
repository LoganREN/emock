package com.mzh.emock.manager.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EMManagerController {

    @RequestMapping("/em/manager/index")
    public String index(){
        return "xxx";
    }
}
