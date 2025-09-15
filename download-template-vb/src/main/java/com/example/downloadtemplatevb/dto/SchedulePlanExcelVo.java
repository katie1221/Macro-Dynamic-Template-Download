package com.example.downloadtemplatevb.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class SchedulePlanExcelVo {

    @Excel(name = "节点编号")
    private Integer nodeNumber;

    @Excel(name = "事项")
    private String matter;

    @Excel(name = "上级节点")
    private Integer supNodeNumber;

    @Excel(name = "标段")
    private String sectionName;

    @Excel(name = "开始时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Excel(name = "完成时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "是否重要")
    private String emphasis;

    @Excel(name = "业务部门")
    private String businessDepartment;

    @Excel(name = "责任部门")
    private String dutyDepartment;

    @Excel(name = "责任人")
    private String dutyUser;

    @Excel(name = "前置节点")
    private String preNodeNumber;

    @Excel(name = "备注")
    private String nodeDescription;
}
