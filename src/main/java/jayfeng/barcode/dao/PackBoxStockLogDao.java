package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.PackBoxStockLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 袋库存拼盒日志数据持久层
 * @author JayFeng
 * @date 2021/10/21
 */
@Repository
public interface PackBoxStockLogDao extends BaseMapper<PackBoxStockLog> {

    /**
     * 根据拼盒编号查询拼盒数据
     * @param packBoxNumber 拼盒编号
     * @return 返回
     */
    @Select("SELECT `id`, `pro_material_number`, `pro_model`, `lot_number`, `amount`, `type`, " +
            "`pack_box_number`, `ware_code`, `ware_name`, `shelf_code`, `create_time`, `update_time` " +
            "FROM `pack_box_stock_log` WHERE `pack_box_number` = #{packBoxNumber}")
    List<PackBoxStockLog> findByPackBoxNumber(@Param("packBoxNumber") String packBoxNumber);

}
