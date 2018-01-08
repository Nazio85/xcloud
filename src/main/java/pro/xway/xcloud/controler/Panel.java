package pro.xway.xcloud.controler;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import pro.xway.xcloud.dao.CategoryRepository;
import pro.xway.xcloud.dao.FileRepository;
import pro.xway.xcloud.dao.UsersRepository;
import pro.xway.xcloud.model.Category;
import pro.xway.xcloud.model.FileXCloud;
import pro.xway.xcloud.model.UserXCloud;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/panel")
public class Panel {
    public static final String ERROR = "Error";
    public static final String MY_FILES = "MyFiles";
    public static final String FAIL_UPLOADS = "Загрузка не удалась";
    public static final String PANEL = "panel";
    public static final String STORAGE = "storage";


    private Category parentCategory;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    MessageSource messageSource;
    @Autowired
    FileRepository fileRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String panel(Model model) {
        loadPanel(model, 0L);
        return "panel";
    }

    @RequestMapping(value = "/category/{id}")
    public String openCategory(@PathVariable String id, Model model) {

        loadPanel(model, parseId(id));
        return "panel";
    }

    @RequestMapping(value = "/category/{currentId}/delete/{id}")
    public String deleteCategory(@PathVariable String currentId, @PathVariable String id, Model model) {
        Long idCat = parseId(id);
        categoryRepository.delete(idCat);

        loadPanel(model, parseId(currentId));
        return "panel";
    }

    @RequestMapping(value = "/category/{currentId}/delete-file/{id}")
    public String deleteFile(@PathVariable String currentId, @PathVariable String id, Model model) {
        Long idFile = parseId(id);
        File file = getFileForBD(fileRepository.findOne(idFile));
        if (file.delete()){
            fileRepository.delete(idFile);
        }

        loadPanel(model, parseId(currentId));
        return "panel";
    }

    @RequestMapping(value = "/category/{id}/newCategory", method = RequestMethod.POST)
    public String newCategory(@PathVariable String id, Model model, @NotNull String newCategory) {
        Long idCat = parseId(id);
        if (!newCategory.equals("")) {
            Category category = new Category(newCategory, idCat, getCurrentUserId());
            categoryRepository.save(category);
        } else model.addAttribute(ERROR, "Enter the name of the parentCategory");

        loadPanel(model, parseId(id));
        return "panel";
    }

    @RequestMapping(value = "/category/{categoryId}/upload", method = RequestMethod.POST)
    public String uploadFile(@PathVariable String categoryId,
                             @RequestParam("file") MultipartFile file, Model model) {
        if (uploadFile(file, parseId(categoryId), model)) {
            FileXCloud fileXCloud = fileRepository.findByNameAndParentId(file.getOriginalFilename(), parseId(categoryId));
            if (fileXCloud == null) {
                fileXCloud = new FileXCloud(file.getOriginalFilename(),
                        parseId(categoryId), getCurrentUserId(), file.getSize());
                fileRepository.save(fileXCloud);
            } else {
                fileXCloud.updateDate();
                fileRepository.save(fileXCloud);
            }

        }
        loadPanel(model, parseId(categoryId));
        return PANEL;
    }

    @RequestMapping(value = "/category/{categoryId}/download/{fileId}")
    public StreamingResponseBody downloadFile(@PathVariable String categoryId, @PathVariable String fileId,
                                              Model model, HttpServletResponse httpResponse) throws IOException {
        FileXCloud fileDB = fileRepository.findOne(parseId(fileId));
        File file = getFileForBD(fileDB);

        InputStream resource = new BufferedInputStream(new FileInputStream(file));
        loadPanel(model, parseId(categoryId));

        String fileName = fileDB.getName();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String mimeType = "application/" + fileType;

        httpResponse.setContentType(mimeType);
        httpResponse.setHeader("Content-Disposition",
                "filename=\"" + MimeUtility.encodeWord(fileName, "utf-8", "Q") + "\"");

        return outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = resource.read(data, 0, data.length)) != -1) {
                System.out.println("Writing some bytes..");
                outputStream.write(data, 0, nRead);
            }
        };
    }


    private boolean uploadFile(MultipartFile file, Long categoryId, Model model) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                File catalog = new File(STORAGE + "/" + getCurrentUserId() + "/" + categoryId);
                catalog.mkdirs();

                File endFile = new File(catalog, file.getOriginalFilename());

                BufferedOutputStream bufferedOutputStream =
                        new BufferedOutputStream(new FileOutputStream(endFile));
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute(messageSource.getMessage("uploadError", null, null));
                return false;
            }
        } else {
            model.addAttribute(messageSource.getMessage("selectTheFile", null, null));
            return false;
        }
    }

    private void loadPanel(Model model, Long idCategory) {
        List<Category> allCategory = getAllCategory();
        model.addAttribute("User", getCurrentUser());
        model.addAttribute("CategoryList", getListCategoryChild(allCategory, idCategory));
        model.addAttribute("CurrentCategory", getCurrentCategory(allCategory, idCategory));
        model.addAttribute("MainCategory", getMainCategoryList(allCategory));
        model.addAttribute("BreadCrumbs", getBreadCrumbs(allCategory, idCategory));
        model.addAttribute("MyFiles", getFilesCurrentCategory(idCategory));
        model.addAttribute("AllSizeFiles", getAllSizeFiles(getAllFilesCurrentUser()));
    }

    private List<FileXCloud> getAllFilesCurrentUser() {
        return fileRepository.findByUserId(getCurrentUserId());
    }

    private int getAllSizeFiles(List<FileXCloud> filesCurrentCategory) {
        int size = 0;
        for (FileXCloud fileXCloud : filesCurrentCategory) {
            size += fileXCloud.getSize();
        }
        size = size/1048576;
        return size;
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

    public static User getCurrentUser() {
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

    private Long parseId(String id) {
        Long l;
        try {
            l = Long.parseLong(id);
        } catch (Exception e) {
            e.printStackTrace();
            l = 0L;
        }
        return l;
    }

    public List<FileXCloud> getFilesCurrentCategory(Long idCategory) {
        List<FileXCloud> list = fileRepository.findByUserIdAndParentId(getCurrentUserId(), idCategory);
        return list;
    }

    public static File getFileForBD(FileXCloud fileDB){
        File file = new File(STORAGE + "/" + fileDB.getUserId()
                + "/" + fileDB.getParentId() + "/" + fileDB.getName());
        return file;
    }
}
