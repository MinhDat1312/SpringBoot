package vn.hoidanit.jobhunter.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.jobhunter.domain.response.ResUploadFileDTO;
import vn.hoidanit.jobhunter.service.FileService;
import vn.hoidanit.jobhunter.util.exception.StorageException;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    @Value("${hoidanit.upload-file.base-uri}")
    private String baseURI;

    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files")
    public ResponseEntity<ResUploadFileDTO> upload(@RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder)
            throws URISyntaxException, IOException, StorageException {
        // validate file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload file");
        }

        List<String> allowedExtensions = Arrays.asList(".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx");
        boolean validated = allowedExtensions.stream()
                .anyMatch(item -> file.getOriginalFilename().toLowerCase().endsWith(item));
        if (validated == false) {
            throw new StorageException("Invalid file extension, only allows: " + allowedExtensions.toString());
        }

        // create folder
        this.fileService.handleCreateFolder(baseURI + "/" + folder);

        // store file
        String finalFileName = this.fileService.handleStoreFile(file, baseURI + "/" + folder);
        ResUploadFileDTO resUploadFileDTO = new ResUploadFileDTO();
        resUploadFileDTO.setFileName(finalFileName);
        resUploadFileDTO.setUploadedAt(Instant.now());

        return ResponseEntity.ok().body(resUploadFileDTO);
    }
}
