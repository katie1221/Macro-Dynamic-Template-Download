package com.example.downloadtemplatevb.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.afterturn.easypoi.handler.inter.IExcelDataHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/***
 * @Author Yuting
 * @Date 2020/7/15
 * excel 导入导出工具类
 */
@Slf4j
public class ExcelUtils {

    /**
     * 导入表格(单sheet)
     *
     * @param filePath   文件路径
     * @param titleRows  标题占几行
     * @param headerRows 头占几行
     * @param claxx      转换的对象
     */
    public static < T > List< T > importExcel(String filePath, Integer titleRows, Integer headerRows, Class< T > claxx) {
        if (StringUtils.isEmpty(filePath)) return null;
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        return ExcelImportUtil.importExcel(new File(filePath), claxx, params);
    }

    /**
     * 导入表格(单sheet)
     *
     * @param file       上传的文件
     * @param claxx      转换的对象
     * @param headerRows excel头占几行
     * @param titleRows  标题占几行
     */
    @SneakyThrows(Exception.class)
    public static < T > List< T > importExcel(MultipartFile file, Integer titleRows, Integer headerRows, Class< T > claxx) {
        if (ObjectUtils.isEmpty(file)) return null;
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        return ExcelImportUtil.importExcel(file.getInputStream(), claxx, params);
    }


    /**
     * 导入表格(多sheet)
     *
     * @param file            上传的文件
     * @param claxx           转换的对象
     * @param headerRows      excel头占几行
     * @param startSheetIndex 第几个sheet
     * @param titleRows       标题占几行
     */
    @SneakyThrows(Exception.class)
    public static < T > List< T > importExcelMore(File file, Integer titleRows, Integer headerRows, Integer startSheetIndex, Class< T > claxx) {
        if (ObjectUtils.isEmpty(file)) return null;
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        params.setStartSheetIndex(startSheetIndex);
        ExcelImportResult< Object > excelImportResult = ExcelImportUtil.importExcelMore(FileUtils.openInputStream(file), claxx, params);
        if (!excelImportResult.isVerifyFail()) return (List< T >) excelImportResult.getList();
        else log.debug("解析失败的列:{}", JsonUtils.buildNonDefaultBinder().toJson(excelImportResult.getFailList()));
        return null;
    }

    /**
     * 导入表格(多sheet)
     * @param file            上传的文件
     * @param claxx           转换的对象
     * @param headerRows      excel头占几行
     * @param startSheetIndex 第几个sheet
     * @param titleRows       标题占几行
     */
    @SneakyThrows(Exception.class)
    public static < T > List< T > importExcelMore(MultipartFile file, Integer titleRows, Integer headerRows, Integer startSheetIndex, Class< T > claxx) {
        List<T> resultInfo = new ArrayList<>();
        try {
            // 根据file得到Workbook,主要是要根据这个对象获取,传过来的excel有几个sheet页
            Workbook workBook = getWorkBook(file);
            StringBuilder sb=new StringBuilder();
            ImportParams params = new ImportParams();
            // 表头在第几行
            params.setTitleRows(titleRows);
            // 距离表头中间有几行不要的数据
            params.setHeadRows(headerRows);
            // 循环工作表Sheet
            for (int numSheet = 0; numSheet < workBook.getNumberOfSheets(); numSheet++) {
                // 第几个sheet页
                params.setStartSheetIndex(numSheet);
                // 验证数据
//                params.setNeedVerify(true);
                ExcelImportResult<T> result =ExcelImportUtil.importExcelMore(file.getInputStream(),
                        claxx, params);
                // 校验是否合格
                if(result.isVerifyFail()){
                    // 不合格的数据
                    List<T> errorList = result.getList();
                    // 拼凑错误信息,自定义
                    for(int i=0;i<errorList.size();i++) {
                        getWrongInfo(sb, errorList, i, errorList.get(i), "name", "用户信息不合法");
                    }
                }
                // 合格的数据
                resultInfo.addAll(result.getList());
            }
        } catch (Exception e) {
            log.error("导入失败:{}",e);
        }
        return resultInfo;
    }

    /**
     * 导入表格(多sheet)
     * @param file            上传的文件
     * @param claxx           转换的对象
     * @param headerRows      excel头占几行
     * @param startSheetIndex 第几个sheet
     * @param titleRows       标题占几行
     * @param excelDataHandler 需要特殊处理得数据类型
     */
    @SneakyThrows(Exception.class)
    public static < T > List< T > importExcelMore(MultipartFile file, Integer titleRows, Integer headerRows, Integer startSheetIndex, Class< T > claxx, IExcelDataHandler<T> excelDataHandler) {
        List<T> resultInfo = new ArrayList<>();
        try {
            // 根据file得到Workbook,主要是要根据这个对象获取,传过来的excel有几个sheet页
            Workbook workBook = getWorkBook(file);
            StringBuilder sb=new StringBuilder();
            ImportParams params = new ImportParams();
            // 表头在第几行
            params.setTitleRows(titleRows);
            // 距离表头中间有几行不要的数据
            params.setHeadRows(headerRows);
            params.setDataHandler(excelDataHandler);
            // 循环工作表Sheet
            for (int numSheet = 0; numSheet < workBook.getNumberOfSheets(); numSheet++) {
                // 第几个sheet页
                params.setStartSheetIndex(numSheet);
                // 验证数据
                params.setNeedVerify(true);
                ExcelImportResult<T> result =ExcelImportUtil.importExcelMore(file.getInputStream(),
                        claxx, params);
                // 校验是否合格
                if(result.isVerifyFail()){
                    // 不合格的数据
                    List<T> errorList = result.getList();
                    // 拼凑错误信息,自定义
                    for(int i=0;i<errorList.size();i++) {
                        getWrongInfo(sb, errorList, i, errorList.get(i), "name", "用户信息不合法");
                    }
                }
                // 合格的数据
                resultInfo.addAll(result.getList());
            }
        } catch (Exception e) {
            log.error("导入失败:{}",e);
        }
        return resultInfo;
    }

    public static void exportExcel(List< ? > list, String title, String sheetName, Class< ? > claxx, String fileName, boolean isCreateHeader, HttpServletResponse response) {
        ExportParams exportParams = new ExportParams(title, sheetName);
        exportParams.setCreateHeadRows(isCreateHeader);
        defaultExport(list, claxx, fileName, response, exportParams);
    }

    public static void exportExcel(List< ? > list, String title, String sheetName, Class< ? > claxx, String fileName, HttpServletResponse response) {
        defaultExport(list, claxx, fileName, response, new ExportParams(title, sheetName));
    }

    public static void exportExcel(List< Map< String, Object > > list, String fileName, HttpServletResponse response) {
        defaultExport(list, fileName, response);
    }

    public static byte[] generateExcel(List< Map< String, Object > > list) {
        Workbook workbook = ExcelExportUtil.exportExcel(list, ExcelType.HSSF);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

//    public static ByteArrayOutputStream exportExcel(Collection< ? > listData, Class< ? > pojoClass, String headTitle, String sheetName,
//                                short height) {
//        ExportParams params = new ExportParams(headTitle,  sheetName);
//        params.setHeight(height);
//        params.setStyle(ExcelStyle.class);
//        params.setType(ExcelType.XSSF);
//        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
//            Workbook workbook = ExcelExportUtil.exportExcel(params, pojoClass, listData);
//            workbook.write(out);
//            return out;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    private static void defaultExport(List< ? > list, Class< ? > claxx, String fileName, HttpServletResponse response, ExportParams exportParams) {
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, claxx, list);
        if (workbook != null) downLoadExcel(fileName, response, workbook);
    }

    @SneakyThrows(Exception.class)
    private static void downLoadExcel(String fileName, HttpServletResponse response, Workbook workbook) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        workbook.write(response.getOutputStream());
    }

    private static void defaultExport(List< Map< String, Object > > list, String fileName, HttpServletResponse response) {
        Workbook workbook = ExcelExportUtil.exportExcel(list, ExcelType.HSSF);
        if (workbook != null) downLoadExcel(fileName, response, workbook);
    }

    private static void defaultExport(Map< String, Object > map, TemplateExportParams params, String fileName, HttpServletResponse response) {
        new TemplateExportParams();
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        if (workbook != null) downLoadExcel(fileName, response, workbook);
    }

    /**
     * 得到Workbook对象
     *
     * @param file 解析的文件对象
     */
    public static Workbook getWorkBook(File file) {
        Workbook hssfWorkbook;
        try (InputStream is = FileUtils.openInputStream(file)) {
            hssfWorkbook = new HSSFWorkbook(is);
        } catch (Exception ex) {
            /***
             * excel 兼容03和07
             */
            try (InputStream is = FileUtils.openInputStream(file)) {
                hssfWorkbook = new XSSFWorkbook(is);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return hssfWorkbook;
    }


    /**
     * 得到Workbook对象
     * @param file
     * @return
     * @throws IOException
     */
    public static Workbook getWorkBook(MultipartFile file) throws IOException {
        //这样写  excel 能兼容03和07
        InputStream is = file.getInputStream();
        Workbook hssfWorkbook = null;
        try {
            hssfWorkbook = new HSSFWorkbook(is);
        } catch (Exception ex) {
            is =file.getInputStream();
            hssfWorkbook = new XSSFWorkbook(is);
        }
        return hssfWorkbook;
    }


    /**
     * 得到错误信息
     * @param sb
     * @param list
     * @param i
     * @param obj
     * @param name  用哪个属性名去表明不和规定的数据
     * @param msg
     * @throws Exception
     */
    public static void getWrongInfo(StringBuilder sb,List list,int i,Object obj,String name,String msg) throws Exception {
        Class clazz = obj.getClass();
        Object str = null;
        //得到属性名数组
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals(name)) {
                //用来得到属性的get和set方法
                PropertyDescriptor pd = new PropertyDescriptor(f.getName(), clazz);
                //得到get方法
                Method getMethod = pd.getReadMethod();
                str = getMethod.invoke(obj);
            }
        }
        if (i == 0) {
            sb.append(msg + str + ";");
        } else if (i == (list.size() - 1)) {
            sb.append(str + "</br>");
        } else {
            sb.append(str + ";");
        }
    }
}