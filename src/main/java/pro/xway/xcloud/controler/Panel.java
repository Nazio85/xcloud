package pro.xway.xcloud.controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pro.xway.xcloud.dao.CategoryRepository;
import pro.xway.xcloud.dao.UsersRepository;
import pro.xway.xcloud.model.Category;
import pro.xway.xcloud.model.UserXCloud;

import java.util.List;

@Controller
@RequestMapping(value = "/panel")
public class Panel {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UsersRepository usersRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String panel(Model model) {
        loadPanel(model);
        return "panel";
    }

    @RequestMapping(value = "/category/{id}")
    @ResponseBody
    public String openCategory(@PathVariable String id, Model model){

        loadPanel(model);
//        return "panel";
        return id;
    }

    @RequestMapping(value = "/category/delete/{id}")
    public String deleteCategory(@PathVariable String id, Model model){
        try {
            Long idCat = Long.parseLong(id);
            categoryRepository.delete(idCat);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            model.addAttribute("Error", "Delete a category is impossible");
        }

        loadPanel(model);
        return "panel";
    }

    @RequestMapping(value = "/newCategory", method = RequestMethod.POST)
    public String newCategory(Model model, String newCategory){
        Category category = new Category(newCategory, 0L, getCurrentUserId());
        categoryRepository.save(category);
        loadPanel(model);
        return "panel";
    }

    private void loadPanel(Model model) {
        model.addAttribute("User", getCurrentUser());
        model.addAttribute("Category", getCategory());
    }

    public User getCurrentUser() {
        User name = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return name;
    }

    public Long getCurrentUserId(){
        UserXCloud userXCloud = usersRepository.findByUsername(getCurrentUser().getUsername());
        return userXCloud.getId();
    }

    public List<Category> getCategory() {
        List<Category> category = categoryRepository.findByUserId(getCurrentUserId());
        return category;
    }
}