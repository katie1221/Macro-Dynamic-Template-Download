package com.example.downloadtemplatevb.util;

import com.example.downloadtemplatevb.dto.SchedulePlanExcelVo;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author qzz
 * @date 2025/9/15
 */
@Component
public class ExcelTemplateDownloadRead {

    /**
     * 读取excel--- 网络excel url
     * @param url
     * @param urlName
     */
    public void importExcelSingleUrl(String url, String urlName) throws Exception {
        String suffix = Objects.requireNonNull(url).substring(url.lastIndexOf(".") + 1);
        if (!(suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx") || suffix.equalsIgnoreCase("xlsm"))) {
            throw new Exception("格式不正确");
        }

        //下载url
        File file = File.createTempFile(urlName.split("\\.")[0], suffix);
        // 确保临时文件在JVM退出时删除
        file.deleteOnExit();

        // 从URL下载文件到临时文件
        url = getNewFileUrl(url);
        try {
            FileUtils.copyURLToFile(new URL(url), file);
        } catch (Exception e) {
            throw new Exception("文件下载失败: " + e.getMessage(), e);
        }

        if ((double) file.length() / (1024 * 1024) > 10) {
            throw new Exception("上传的文件不能操作10MB");
        }

        //读取excel
        List<SchedulePlanExcelVo> constructionTaskExcelVoList = ExcelUtils.importExcelMore(file, 0, 1, 0, SchedulePlanExcelVo.class);

        System.out.println(constructionTaskExcelVoList);
    }

    /**
     * 含中文参数，重新编码URL中的中文参数
     * @param fileUrl
     * @return
     */
    public static String getNewFileUrl(String fileUrl) {
        // 1. 检查并编码URL中的中文参数（关键修复）
        try {
            URL originalUrl = new URL(fileUrl);
            String query = originalUrl.getQuery();
            if (query != null && !query.isEmpty()) {
                // 分割参数并编码中文值
                String[] params = query.split("&");
                StringBuilder encodedQuery = new StringBuilder();
                for (String param : params) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2) {
                        // 对参数值进行UTF-8编码
                        String encodedValue = URLEncoder.encode(keyValue[1], StandardCharsets.UTF_8.name());
                        encodedQuery.append(keyValue[0]).append("=").append(encodedValue).append("&");
                    } else {
                        encodedQuery.append(param).append("&");
                    }
                }
                // 重建编码后的URL
                if (encodedQuery.length() > 0) {
                    encodedQuery.setLength(encodedQuery.length() - 1); // 移除最后一个&
                    String encodedUrl = originalUrl.getProtocol() + "://" +
                            originalUrl.getHost() +
                            (originalUrl.getPort() != -1 ? ":" + originalUrl.getPort() : "") +
                            originalUrl.getPath() + "?" + encodedQuery;
                    fileUrl = encodedUrl;
                }
            }
        } catch (Exception e) {
            System.err.println("URL编码处理失败: " + e.getMessage());
        }
        return  fileUrl;
    }
}
