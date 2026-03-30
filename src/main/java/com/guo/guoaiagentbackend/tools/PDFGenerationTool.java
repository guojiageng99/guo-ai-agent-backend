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
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class PDFGenerationTool {

    private final String publicBaseUrl;
    private final String downloadPathPrefix;

    /**
     * @param publicBaseUrl  生产环境可配置完整站点地址（无尾斜杠），便于在回复中给出可复制的绝对 URL；空则只返回相对路径
     * @param servletContextPath 如 /api，与 {@code server.servlet.context-path} 一致
     */
    public PDFGenerationTool(String publicBaseUrl, String servletContextPath) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        this.publicBaseUrl = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String ctx = servletContextPath == null || servletContextPath.isBlank() ? "" : servletContextPath.trim();
        if (ctx.endsWith("/")) {
            ctx = ctx.substring(0, ctx.length() - 1);
        }
        this.downloadPathPrefix = ctx + "/files/pdf";
    }

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

    @Tool(description = "根据纯文本生成 PDF（正文请使用简体中文）。成功时返回下载路径（含 /api/files/pdf/ 前缀，供前端显示下载按钮）及磁盘路径。"
            + "fileName 需带 .pdf 后缀，勿含路径符号，例如 beijing_yuehui.pdf。")
    public String generatePDF(
            @ToolParam(description = "PDF 文件名，例如 plan.pdf") String fileName,
            @ToolParam(description = "写入 PDF 的正文，须为简体中文纯文本") String content) {
        if (!isSafePdfFileName(fileName)) {
            return "fileName 非法：须以 .pdf 结尾且不能含路径符号（/、\\、..）。";
        }
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
            String encoded = UriUtils.encodePathSegment(fileName, StandardCharsets.UTF_8);
            String relDownload = downloadPathPrefix + "/" + encoded;
            String abs = Path.of(filePath).toAbsolutePath().normalize().toString();
            String urlLine = publicBaseUrl.isEmpty()
                    ? relDownload
                    : publicBaseUrl + relDownload;
            return "PDF 已生成。用户登录后可在 Manus 对话中点击「下载 PDF」按钮，或访问：" + urlLine
                    + " （须在请求头携带 Authorization: Bearer 令牌；应用内按钮会自动携带）。服务端文件：" + abs;
        } catch (IOException e) {
            Throwable c = e.getCause();
            String detail = c != null ? e.getMessage() + "；原因：" + c.getMessage() : e.getMessage();
            return "生成 PDF 失败：" + detail + "（请检查进程对目录 " + fileDir + " 的写权限，以及磁盘空间）";
        }
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
}
