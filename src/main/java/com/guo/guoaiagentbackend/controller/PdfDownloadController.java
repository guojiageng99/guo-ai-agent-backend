package com.guo.guoaiagentbackend.controller;

import com.guo.guoaiagentbackend.constant.FileConstant;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manus 等工具生成的 PDF 位于 {@code tmp/pdf/}，通过本接口在登录后下载/浏览器内查看。
 */
@RestController
@RequestMapping("/files")
public class PdfDownloadController {

    @GetMapping("/pdf/{fileName:.+}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable("fileName") String fileName) {
        if (!isSafePdfFileName(fileName)) {
            return ResponseEntity.badRequest().build();
        }
        Path baseDir = Path.of(FileConstant.FILE_SAVE_DIR, "pdf").toAbsolutePath().normalize();
        Path file = baseDir.resolve(fileName).normalize();
        if (!file.startsWith(baseDir) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource body = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + escapeFilename(fileName) + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }

    private static boolean isSafePdfFileName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            return false;
        }
        return !name.contains("..") && name.indexOf('/') < 0 && name.indexOf('\\') < 0;
    }

    private static String escapeFilename(String name) {
        return name.replace("\"", "'");
    }
}
