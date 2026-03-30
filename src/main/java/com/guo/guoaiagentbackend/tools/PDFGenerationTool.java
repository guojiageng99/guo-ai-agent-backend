package com.guo.guoaiagentbackend.tools;

import cn.hutool.core.io.FileUtil;
import com.guo.guoaiagentbackend.constant.FileConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Path;

public class PDFGenerationTool {

    /**
     * UniGB-UCS2-H 等编码仅支持 BMP 平面；搜索结果里若含表情符号等会触发 “only accepts BMP codepoints”。
     */
    private static String stripNonBmpForPdf(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        text.codePoints().forEach(cp -> {
            if (cp <= 0xFFFF) {
                sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
    }

    @Tool(description = "根据纯文本生成 PDF（正文请使用简体中文）。成功时返回保存的绝对路径，目录为项目根下 tmp/pdf/。"
            + "fileName 需带 .pdf 后缀，例如 beijing_yuehui.pdf。")
    public String generatePDF(
            @ToolParam(description = "PDF 文件名，例如 plan.pdf") String fileName,
            @ToolParam(description = "写入 PDF 的正文，须为简体中文纯文本") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
//                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
//                        .toAbsolutePath().toString();
//                PdfFont font = PdfFontFactory.createFont(fontPath,
//                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                String safe = stripNonBmpForPdf(content);
                Paragraph paragraph = new Paragraph(safe);
                // 添加段落并关闭文档
                document.add(paragraph);
            }
            return "PDF 已生成，路径：" + Path.of(filePath).toAbsolutePath().normalize();
        } catch (IOException e) {
            return "生成 PDF 失败：" + e.getMessage();
        }
    }
}
