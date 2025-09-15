package com.example.downloadtemplatevb.util;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 通用带宏的Excel模板处理器
 *  支持读取带宏的模板文件、填充数据、设置下拉框等功能
 *  可通过配置适配不同类型的数据和模板结构
 * @author qzz
 * @date 2025/9/15
 */
@Slf4j
@Component
public class GeneralExcelTemplateHandler {

    /**
     * 处理带宏的Excel模板并填充数据
     *
     * @param templatePath  模板文件路径
     * @param dataList      要填充的数据列表
     * @param columnMapper  列名到数据字段的映射函数
     * @param headerRowIndex 表头所在行索引
     * @param mainSheetIndex 主表所在索引
     * @param <T>           数据类型
     * @return 处理后的Excel字节流
     */
    public <T> byte[] processTemplate(String templatePath, List<T> dataList, Function<T, Map<String, Object>> columnMapper, int headerRowIndex, int mainSheetIndex) {
        //读取已有的带宏模板
        try (InputStream is = new FileInputStream(templatePath);
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {

            // 获取主工作表
            Sheet mainSheet = workbook.getSheetAt(mainSheetIndex);
            if (mainSheet == null) {
                throw new RuntimeException("未找到索引为 " + mainSheetIndex + " 的工作表");
            }

            // 填充主表数据
            if(CollUtil.isNotEmpty(dataList)){
                fillMainTableData(mainSheet, dataList, columnMapper, headerRowIndex);
            }

            // 设置部门和人员数据（可根据需要调整或做成可配置）
            setDepartmentAndPersonData(workbook);

            // 转换为字节流
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                return bos.toByteArray();
            }
        } catch (Exception e) {
            log.error("处理Excel模板失败", e);
            throw new RuntimeException("处理Excel模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重载方法，使用默认配置
     */
    public <T> byte[] processTemplate(String templatePath) {
        return processTemplate(templatePath, null, null, 0, 0);
    }

    /**
     * 重载方法，使用默认配置
     */
    public <T> byte[] processTemplate(String templatePath, List<T> dataList, Function<T, Map<String, Object>> columnMapper) {
        return processTemplate(templatePath, dataList, columnMapper, 0, 0);
    }

    /**
     * 填充主表数据,优化新建行的样式继承
     *
     * @param mainSheet     主工作表
     * @param dataList      数据列表
     * @param columnMapper  列映射函数
     * @param headerRowIndex 表头行索引
     * @param <T>           数据类型
     */
    private <T> void fillMainTableData(Sheet mainSheet, List<T> dataList, Function<T, Map<String, Object>> columnMapper, int headerRowIndex) {
        // 获取表头行
        Row headerRow = mainSheet.getRow(headerRowIndex);
        if (headerRow == null) {
            throw new RuntimeException("在索引 " + headerRowIndex + " 处未找到表头行");
        }

        // 遍历数据列表，逐行填充
        for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {
            T data = dataList.get(rowIndex);
            int excelRowNum = rowIndex + headerRowIndex + 1; // 数据行从表头下一行开始

            // 获取或创建当前行
            Row row = mainSheet.getRow(excelRowNum);
            if (row == null) {
                row = mainSheet.createRow(excelRowNum);
                // 优化：新建行时，复制上一行（模板已有行）的列宽和样式
                copyRowStyleAndWidth(mainSheet, excelRowNum - 1, excelRowNum);
            } else {
                // 清除行中已有内容
                clearRow(row);
            }

            // 填充当前行数据
            fillRowData(row, headerRow, data, columnMapper);
        }

        // 清除多余的旧数据行
        clearExtraRows(mainSheet, dataList.size() + headerRowIndex + 1);
    }

    /**
     * 复制指定行的样式和列宽到目标行（用于新建行继承模板样式）
     * @param sheet 工作表
     * @param sourceRowNum 源行号（模板已有行）
     * @param targetRowNum 目标行号（新建行）
     */
    private void copyRowStyleAndWidth(Sheet sheet, int sourceRowNum, int targetRowNum) {
        Row sourceRow = sheet.getRow(sourceRowNum);
        Row targetRow = sheet.getRow(targetRowNum);
        if (sourceRow == null || targetRow == null) {
            return;
        }

        // 1. 复制列宽
        for (int colIndex = 0; colIndex < sourceRow.getLastCellNum(); colIndex++) {
            int colWidth = sheet.getColumnWidth(colIndex);
            if (colWidth > 0) {
                sheet.setColumnWidth(colIndex, colWidth);
            }
        }

        // 2. 复制单元格样式（提前为目标行创建单元格并绑定样式）
        for (int colIndex = 0; colIndex < sourceRow.getLastCellNum(); colIndex++) {
            Cell sourceCell = sourceRow.getCell(colIndex);
            if (sourceCell != null) {
                Cell targetCell = targetRow.getCell(colIndex);
                if (targetCell == null) {
                    targetCell = targetRow.createCell(colIndex);
                }
                // 直接复用源单元格样式（模板样式）
                targetCell.setCellStyle(sourceCell.getCellStyle());
            }
        }
    }

    /**
     * 填充行数据
     */
    private <T> void fillRowData(Row row, Row headerRow, T data, Function<T, Map<String, Object>> columnMapper) {
        Map<String, Object> dataMap = columnMapper.apply(data);
        if (dataMap == null) {
            log.warn("数据映射结果为null，跳过此行数据");
            return;
        }

        // 遍历表头列
        for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
            Cell headerCell = headerRow.getCell(colIndex);
            if (headerCell == null) {
                continue;
            }

            String columnName = getCellStringValue(headerCell).trim();
            Object value = dataMap.get(columnName);

            if (value != null) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }
                setCellValue(cell, value);
            }
        }
    }

    /**
     * 设置单元格值，支持多种数据类型
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        Workbook workbook = cell.getSheet().getWorkbook();
        CellStyle originalStyle = cell.getCellStyle();

        try {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else if (value instanceof Date) {
                // 保留原有样式，只修改数据格式
                CellStyle style = workbook.createCellStyle();
                style.cloneStyleFrom(originalStyle);

                DataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat("yyyy/mm/dd"));
                cell.setCellStyle(style);
                cell.setCellValue((Date) value);
            } else {
                // 其他类型转为字符串
                cell.setCellValue(value.toString());
            }
        } catch (Exception e) {
            log.error("设置单元格值失败，值: {}, 类型: {}", value, value.getClass().getName(), e);
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 清除行数据，但保留单元格样式（用于清除旧数据时不破坏模板格式）
     */
    private void clearRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                // 只清空值，保留样式
                cell.setCellValue("");
            }
        }
    }

    /**
     * 清除多余的旧数据行
     */
    private void clearExtraRows(Sheet sheet, int startRowNum) {
        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum >= startRowNum) {
            for (int i = lastRowNum; i >= startRowNum; i--) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    sheet.removeRow(row);
                }
            }
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 动态设置责任部门和责任人的可选数据
     * @param workbook
     */
    private void setDepartmentAndPersonData(XSSFWorkbook workbook) {

        String sheetName = "Sheet2";
        //获取或创建数据工作表  果不存在，创建新的隐藏工作表；如果存在，获取该工作表
        XSSFSheet dataSheet = workbook.getSheet(sheetName);
        if (dataSheet == null) {
            dataSheet = workbook.createSheet(sheetName);
        } else {
            // 清空原有数据（可选，根据需求决定是否重置内容）
            // 这里简单示例：删除工作表中所有行（表头外的内容）
            for (int i = dataSheet.getPhysicalNumberOfRows() - 1; i > 0; i--) {
                dataSheet.removeRow(dataSheet.getRow(i));
            }
        }

        // 设置工作表为隐藏（如果是新创建的，或者需要保持隐藏）
        workbook.setSheetHidden(workbook.getSheetIndex(dataSheet), Boolean.TRUE);
        // 隐藏配置工作表（如果存在）
        int configSheetIndex = workbook.getSheetIndex("Config");
        if (configSheetIndex != -1) {
            workbook.setSheetHidden(configSheetIndex, true);
        }

        // 写入部门数据
        String[] departments = {"技术部", "财务部", "人力资源部", "市场部", "开发部"};
        for (int i = 0; i < departments.length; i++) {
            XSSFRow row = dataSheet.getRow(i);
            if (row == null) {
                row = dataSheet.createRow(i);
            }
            row.createCell(0).setCellValue(departments[i]);
        }

        // 写入人员数据（假设人员数据数组，这里示例简单写几个）
        String[] persons = {"张三", "李四", "王五", "赵六"};
        for (int i = 0; i < persons.length; i++) {
            XSSFRow row = dataSheet.getRow(i);
            if (row == null) {
                row = dataSheet.createRow(i);
            }
            row.createCell(1).setCellValue(persons[i]);
        }

    }
}
