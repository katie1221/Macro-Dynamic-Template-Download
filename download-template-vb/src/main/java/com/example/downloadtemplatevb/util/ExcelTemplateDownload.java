package com.example.downloadtemplatevb.util;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现下载动态 Excel 模板（包含单选下拉框、动态多选数据）
 * @author qzz
 * @date 2025/9/15
 */
@Component
public class ExcelTemplateDownload {

    // 模拟从数据库/接口获取“责任部门”和“责任人”数据
    public static List<String> getDeptList() {
        List<String> deptList = new ArrayList<>();
        deptList.add("开发部");
        deptList.add("产品部");
        deptList.add("设计部");
        deptList.add("测试部");
        deptList.add("筑安科技有限公司");
        return deptList;
    }

    public static List<String> getPersonList() {
        List<String> personList = new ArrayList<>();
        personList.add("苏影");
        personList.add("张三");
        personList.add("李四");
        personList.add("王五");
        return personList;
    }

    // 下载 Excel 模板的方法
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 1. 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("项目任务模板");

        // 2. 创建单元格样式（可选，用于美化表头）
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        // 3. 写入表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"节点编号", "事项", "上级节点", "标段", "开始时间", "完成时间", "状态", "是否重要", "业务部门", "责任部门", "责任人", "前置节点", "备注"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
//            // 设置列宽（根据内容调整）
//            sheet.setColumnWidth(i, 25 * 256);
        }

        // 4. 设置“状态”列的单选下拉（行范围：1-100，列：6）
        String[] statusOptions = {"进行中", "已完成"};
        setSingleSelectDropDown(sheet, 1, 100, 6, statusOptions);

        // 5. 设置“是否重要”列的单选下拉（行范围：1-100，列：7）
        String[] importantOptions = {"是", "否"};
        setSingleSelectDropDown(sheet, 1, 100, 7, importantOptions);

        // 6. 设置“责任部门”列的多选下拉（行范围：1-100，列：9）
        List<String> deptList = getDeptList();
        setMultiSelectDropDown(sheet, 1, 100, 9, deptList);

        // 7. 设置“责任人”列的多选下拉（行范围：1-100，列：10）
        List<String> personList = getPersonList();
        setMultiSelectDropDown(sheet, 1, 100, 10, personList);

        // 8. 配置 HTTP 响应，下载 Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("template.xlsm", "UTF-8"));

        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    // 工具方法：设置“单选”下拉列表
    private void setSingleSelectDropDown(Sheet sheet, int firstRow, int lastRow, int columnIndex, String[] options) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(options);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    // 工具方法：设置“多选”下拉列表（需结合 Excel 允许多选的设置，POI 仅配置下拉源）
    private void setMultiSelectDropDown(Sheet sheet, int firstRow, int lastRow, int columnIndex, List<String> options) {
        String[] optionsArr = options.toArray(new String[0]);
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(optionsArr);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
//        // 兼容 Excel 多选（需用户在 Excel 中开启“允许多选”，POI 无法直接设置此属性）
//        if (validation instanceof XSSFDataValidation) {
//            XSSFDataValidation xssfValidation = (XSSFDataValidation) validation;
//            xssfValidation.setSuppressDropDownArrow(false);
//        }
        sheet.addValidationData(validation);
    }
}
