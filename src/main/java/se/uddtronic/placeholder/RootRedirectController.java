package se.uddtronic.placeholder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {
    @GetMapping("/")
    public String redirectToUi() {
        return "redirect:/_placeholder/";
    }
}
