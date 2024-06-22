package vn.hoidanit.jobhunter.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    public void handleCreateFolder(String folder) throws URISyntaxException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);
        File file = new File(path.toString());

        if (!file.isDirectory()) {
            try {
                Files.createDirectory(file.toPath());
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + file.toPath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }
    }

    public String handleStoreFile(MultipartFile file, String folder) throws URISyntaxException, IOException {
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        URI uri = new URI(folder + "/" + finalName);
        Path path = Paths.get(uri);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return finalName;
    }
}
