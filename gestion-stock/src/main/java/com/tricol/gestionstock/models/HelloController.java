package com.tricol.gestionstock.models;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HelloController {
String name;
    @GetMapping(path = "/abdo")

    public String Sayname(String name){
    return "hello " + name +" i hop ur doing good";
}
}
