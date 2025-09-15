package com.example.downloadtemplatevb.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author qzz
 * @date 2025/9/15
 */
@Data
public class ExportSchedulePlanVo {

    /**
     * 节点编号
     **/
    private Integer nodeNumber;

    /**
     * 事项
     **/
    private String matter;

    /**
     * 上级节点
     **/
    private Integer supNodeNumber;

    /**
     * 标段
     **/
    private String sectionName;

    /**
     * 开始时间
     **/
    private Date startTime;

    /**
     * 完成时间
     **/
    private Date endTime;

    /**
     * 状态 1=未开始，2=进行中，3=已完成
     **/
    private String status;

    /**
     * 是否重要
     **/
    private Boolean emphasisFlag;

    /**
     * 业务部门
     **/
    private String businessDepartment;

    /**
     * 责任部门
     **/
    private String dutyDepartment;

    /**
     * 责任人
     **/
    private String dutyUser;

    /**
     * 前置节点
     **/
    private String preNodeNumber;

    /**
     * 备注
     **/
    private String nodeDescription;
}
