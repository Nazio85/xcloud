package pro.xway.xcloud.controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pro.xway.xcloud.dao.CategoryRepository;
import pro.xway.xcloud.dao.UsersRepository;
import pro.xway.xcloud.model.Category;
import pro.xway.xcloud.model.UserXCloud;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/panel")
public class Panel {
    public static final String ERROR = "Error";
    public static final String MY_FILES = "MyFiles";

    private Category parentCategory;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    MessageSource messageSource;

    @RequestMapping(method = RequestMethod.GET)
    public String panel(Model model) {
        loadPanel(model, 0L);
        return "panel";
    }

    @RequestMapping(value = "/category/{id}")
    public String openCategory(@PathVariable String id, Model model) {

        loadPanel(model, parseCategoryId(id));
        return "panel";
    }

    @RequestMapping(value = "/category/{currentId}/delete/{id}")
    public String deleteCategory(@PathVariable String currentId, @PathVariable String id, Model model) {
        Long idCat = parseCategoryId(id);
        categoryRepository.delete(idCat);

        loadPanel(model, parseCategoryId(currentId));
        return "panel";
    }

    @RequestMapping(value = "/category/{id}/newCategory", method = RequestMethod.POST)
    public String newCategory(@PathVariable String id, Model model, @NotNull String newCategory) {
        Long idCat = parseCategoryId(id);
        if (!newCategory.equals("")) {
            Category category = new Category(newCategory, idCat, getCurrentUserId());
            categoryRepository.save(category);
        } else model.addAttribute(ERROR, "Enter the name of the parentCategory");

        loadPanel(model, parseCategoryId(id));
        return "panel";
    }

    private void loadPanel(Model model, Long idCategory) {
        List<Category> allCategory = getAllCategory();
        model.addAttribute("User", getCurrentUser());
        model.addAttribute("CategoryList", getListCategoryChild(allCategory, idCategory));
        model.addAttribute("CurrentCategory", getCurrentCategory(allCategory, idCategory));
        model.addAttribute("MainCategory", getMainCategoryList(allCategory));
        model.addAttribute("BreadCrumbs", getBreadCrumbs(allCategory, idCategory));
    }

    private Category getCurrentCategory(List<Category> allCategory, Long idCategory) {
        for (Category category : allCategory) {
            if (category.getId().equals(idCategory)) return category;
        }
        return getParentCategory();
    }

    private Category getParentCategory() {
        if (parentCategory == null) {
            parentCategory = new Category(MY_FILES, 0L, getCurrentUserId());
            parentCategory.setId(0L);
        }
        return parentCategory;
    }

    private List<Category> getListCategoryChild(List<Category> allCategory, Long idCategory) {
        List<Category> currentCategory = new ArrayList<>();
//        Long parentId = 0L;
//
//        for (Category parentCategory : allCategory) {
//            if (parentCategory.getId().equals(idCategory)){
//                parentId = parentCategory.getId();
//                break;
//            }
//        }
        for (Category category : allCategory) {
            if (category.getParent().equals(idCategory))
                currentCategory.add(category);
        }
        return currentCategory;
    }

    private List<Category> getBreadCrumbs(List<Category> allCategory, Long idCategory) {
        List<Category> categoryList = new ArrayList<>();

        breadCrumbsRecurs(allCategory, categoryList, idCategory);
        categoryList.add(getCurrentCategory(allCategory, idCategory));
        return categoryList;
    }

    private Category breadCrumbsRecurs(List<Category> allCategory, List<Category> categoryList,
                                       Long idCategory) {
        Category cate = null;
        if (idCategory == 0) {
            return getParentCategory();
        } else {
            for (Category cat : allCategory) {
                if (cat.getId().equals(idCategory)) {
                    categoryList.add(breadCrumbsRecurs(allCategory, categoryList, cat.getParent()));
                    cate = cat;
                    break;
                }
            }
        }
        return cate;
    }

    public User getCurrentUser() {
        User name = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return name;
    }

    public Long getCurrentUserId() {
        UserXCloud userXCloud = usersRepository.findByUsername(getCurrentUser().getUsername());
        return userXCloud.getId();
    }

    public List<Category> getAllCategory() {
        return categoryRepository.findByUserId(getCurrentUserId());
    }

    private List<Category> getMainCategoryList(List<Category> category) {
        List<Category> list = new ArrayList<>();
        for (Category cat : category) {
            if (cat.getParent() == 0) list.add(cat);
        }
        return list;
    }

    private Long parseCategoryId(String id) {
        Long l;
        try {
            l = Long.parseLong(id);
        } catch (Exception e) {
            e.printStackTrace();
            l = 0L;
        }
        return l;
    }
}
