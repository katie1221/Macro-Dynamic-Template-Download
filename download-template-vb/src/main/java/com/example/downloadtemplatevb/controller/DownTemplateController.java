package com.example.downloadtemplatevb.controller;

import com.example.downloadtemplatevb.dto.ExportSchedulePlanVo;
import com.example.downloadtemplatevb.util.ExcelTemplateDownload;
import com.example.downloadtemplatevb.util.ExcelTemplateDownloadRead;
import com.example.downloadtemplatevb.util.GeneralExcelTemplateHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;

/**
 * excel 动态模板下载、带宏动态模板下载
 * @author qzz
 * @date 2025/9/15
 */
@RestController
public class DownTemplateController {
    @Autowired
    private ExcelTemplateDownload excelTemplateDownload;
    @Autowired
    private GeneralExcelTemplateHandler generalExcelTemplateHandler;
    @Autowired
    private ExcelTemplateDownloadRead excelTemplateDownloadRead;

    /**
     * 实现下载动态 Excel 模板（包含动态表头、单选下拉框、动态多选数据）
     * @param response
     * @throws IOException
     */
    @GetMapping("/download/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        excelTemplateDownload.downloadTemplate(response);
    }

    /**
     * 下载带宏的Excel模板
     * @param response
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate2(HttpServletResponse response) {
        try {
            // 1. 读取本地文件系统中的模板文件
            // 注意：实际应用中建议将路径配置在配置文件中，避免硬编码
            String templatePath = "F:\\template.xlsm";
            File templateFile = new File(templatePath);

            // 检查文件是否存在
            if (!templateFile.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "模板文件不存在");
                return;
            }

            // 2. 处理模板
            byte[] excelData = generalExcelTemplateHandler.processTemplate(templateFile.getAbsolutePath());

            // 3. 设置响应头
            response.setContentType("application/vnd.ms-excel.sheet.macroEnabled.12");
            response.setHeader("Content-Disposition", "attachment;filename=dynamic_template1.xlsm");
            response.setContentLength(excelData.length);

            // 4. 写入响应流
            OutputStream os = response.getOutputStream();
            os.write(excelData);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "模板处理失败");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * 下载带宏的Excel模板,动态填充数据
     * @param response
     */
    @GetMapping("/downloadTemplate3")
    public void downloadTemplate3(HttpServletResponse response) {
        try {
            // 1. 读取本地文件系统中的模板文件
            // 注意：实际应用中建议将路径配置在配置文件中，避免硬编码
            String templatePath = "F:\\template.xlsm";
            File templateFile = new File(templatePath);

            // 检查文件是否存在
            if (!templateFile.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "模板文件不存在");
                return;
            }

            List<ExportSchedulePlanVo> dataList = createTestDataList(6);

            // 创建列映射函数
            Function<ExportSchedulePlanVo, Map<String, Object>> columnMapper = data -> {
                Map<String, Object> map = new HashMap<>();
                map.put("节点编号", data.getNodeNumber());
                map.put("事项", data.getMatter());
                map.put("上级节点", data.getSupNodeNumber());
                map.put("标段", data.getSectionName());
                map.put("开始时间", data.getStartTime());
                map.put("完成时间", data.getEndTime());
                map.put("状态", data.getStatus());
                map.put("是否重要", data.getEmphasisFlag());
                map.put("业务部门", data.getBusinessDepartment());
                map.put("责任部门", data.getDutyDepartment());
                map.put("责任人", data.getDutyUser());
                map.put("前置节点", data.getPreNodeNumber());
                map.put("备注", data.getNodeDescription());
                return map;
            };

            // 2. 处理模板
            byte[] excelData = generalExcelTemplateHandler.processTemplate(templateFile.getAbsolutePath(), dataList, columnMapper);

            // 3. 设置响应头
            response.setContentType("application/vnd.ms-excel.sheet.macroEnabled.12");
            response.setHeader("Content-Disposition", "attachment;filename=dynamic_template1.xlsm");
            response.setContentLength(excelData.length);

            // 4. 写入响应流
            OutputStream os = response.getOutputStream();
            os.write(excelData);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "模板处理失败");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 创建测试数据列表
     */
    private List<ExportSchedulePlanVo> createTestDataList(int size) {
        List<ExportSchedulePlanVo> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ExportSchedulePlanVo data = new ExportSchedulePlanVo();
            data.setNodeNumber(i + 1);
            data.setMatter("测试事项" + (i + 1));
            data.setSectionName("测试标段");
            data.setStartTime(new Date());
            data.setEndTime(new Date());
            data.setStatus("未开始");
            data.setEmphasisFlag(Boolean.TRUE);
            data.setBusinessDepartment("工管部");
            data.setDutyDepartment("技术部");
            data.setDutyUser("张三");
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 读取excel--- 网络excel url
     * @param url
     * @param urlName
     * @throws IOException
     */
    @GetMapping("/read/template")
    public void readTemplate(@RequestParam("url") String url, @RequestParam("url_name") String urlName) throws Exception {
        url = "http://10.1.102.43:30080/api-file/file/show/?signature=5c133df5fedddb61566727465a45ef928dea054c106794a365c8df7e3d994885123822f0e21b2ebfc229233168a2a5214830cba427b7d13b869fd7421619ee698e8bcc55e26011a620ac88a07f04d483f35957e79ab678117c8337f350581afb&fileName=动态工筹模板_2025_9_11 17_27_32.xls";
        urlName = "动态模板_2025_9_11 17_27_32.xls";
        excelTemplateDownloadRead.importExcelSingleUrl(url, urlName);
    }
}
