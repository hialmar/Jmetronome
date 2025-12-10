package net.torguet.spring;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

   @RequestMapping("/jmetro")
   public String index() {
       return "index";
   }
}
