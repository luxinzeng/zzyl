package com.zzyl.nursing.vo;

import com.zzyl.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 护理等级对象 nursing_level
 *
 * @author luxingzeng
 * @date 2026-10-02
 */
@Data
@ApiModel("护理等级")
public class NursingLevelVo
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 等级名称 */
    @Excel(name = "等级名称")
    @ApiModelProperty("等级名称")
    private String name;

    /** 护理计划ID */
    @Excel(name = "护理计划ID")
    @ApiModelProperty("护理计划ID")
    private Long planId;

    /** 护理计划名称 */
    @ApiModelProperty("护理计划名称")
    private String planName;

    /** 护理费用 */
    @Excel(name = "护理费用")
    @ApiModelProperty("护理费用")
    private BigDecimal fee;

    /** 状态（0：禁用，1：启用） */
    @Excel(name = "状态", readConverterExp = "0=：禁用，1：启用")
    @ApiModelProperty("状态（0：禁用，1：启用）")
    private Integer status;

    /** 等级说明 */
    @ApiModelProperty("等级说明")
    private String description;
}
