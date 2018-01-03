package pro.xway.xcloud.controler;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping("/")
    public String startApp(Model model) {
        return "start";
    }

    @RequestMapping("/login")
    public String login(){
        return "login";
    }





}
