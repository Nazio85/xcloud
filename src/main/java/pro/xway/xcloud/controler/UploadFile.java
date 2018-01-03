package pro.xway.xcloud.controler;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
@RequestMapping ("/upload")
public class UploadFile {
    public static final String FAIL_UPLOADS = "Загрузка не удалась";

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String panel(@RequestParam("file") MultipartFile file,
                        Model model) {
        String uploadFile = uploadFile(file);
        model.addAttribute("Файл загружен", uploadFile);
        return "panel";
    }

    private String uploadFile(MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                File catalog = new File("upload");
                catalog.mkdirs();

                BufferedOutputStream bufferedOutputStream =
                        new BufferedOutputStream(new FileOutputStream(
                                new File(catalog, file.getOriginalFilename())));
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.close();
                return "Ok";
            } catch (IOException e) {
                e.printStackTrace();
                return FAIL_UPLOADS;
            }
        } else {
            return FAIL_UPLOADS + " - Файл пуст";
        }
    }
}