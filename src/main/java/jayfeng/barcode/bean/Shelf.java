package jayfeng.barcode.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库货架实体类
 * @author JayFeng
 * @date 2021/10/20
 */
@Data
public class Shelf {

    /**
     * id
     */
    @TableId(type= IdType.AUTO)
    private Integer id;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 货架类型
     * 0 -- 袋货架
     * 1 -- 盒货架
     * 2 -- 箱货架
     */
    private Byte shelfType;

    /**
     * 货架编号
     */
    private Integer shelfNumber;

    /**
     * 所在仓库的仓库编码
     */
    private String wareCode;

    /**
     * 所在仓库名称
     */
    private String wareName;

    /**
     * 货架二维码
     * 货架类型_货架编码_仓库编码
     */
    private String shelfQrCode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
