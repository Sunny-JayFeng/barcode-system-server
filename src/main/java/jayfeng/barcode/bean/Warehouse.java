package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库实体类
 * @author JayFeng
 * @date 2021/10/21
 */
@Data
public class Warehouse {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 仓库编码
     */
    private String wareCode;

    /**
     * 仓库名称：良品仓、不良品仓、检测仓、出货仓
     * 产品入库，需要先到检测仓，然后良品不良品分别进入不同的仓
     * 出货的时候，需要先从良品仓到出货仓
     * 如果是调拨入库，即：从某个良品仓调到另外一个良品仓，则不需要检测
     */
    private String wareName;

    /**
     * 仓库管理员
     */
    private String manager;

    /**
     * 仓库二维码：仓库管理员-仓库编码
     */
    private String warehouseQrCode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
