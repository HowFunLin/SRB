package com.atguigu.srb.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 数据字典
 * </p>
 *
 * @author Riyad
 * @since 2021-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Dict对象", description = "数据字典")
public class Dict implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(name = "parent_id")
    @ApiModelProperty(value = "上级id")
    private Long parentId;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "值")
    private Integer value;

    @Column(name = "dict_code")
    @ApiModelProperty(value = "编码")
    private String dictCode;

//    @Column(name = "create_time") // 不适配 LocalDateTime
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

//    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "删除标记（0:不可用 1:可用）")
    @Column(name = "is_deleted")
    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;

    @Transient
    @TableField(exist = false) // 不存在表中
    private Boolean hasChildren;
}
