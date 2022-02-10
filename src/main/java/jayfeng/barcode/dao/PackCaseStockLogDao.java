package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.PackCaseStockLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 盒库存拼箱日志数据持久层
 * @author JayFeng
 * @date 2021/10/21
 */
@Repository
public interface PackCaseStockLogDao extends BaseMapper<PackCaseStockLog> {

    /**
     * 根据拼箱编号查询拼箱数据
     * @param packCaseNumber 拼箱编号
     * @return 返回
     */
    @Select("SELECT `id`, `pro_material_number`, `pro_model`, `lot_number`, `amount`, `type`, " +
            "`pack_case_number`, `ware_code`, `ware_name`, `shelf_code`, `create_time`, `update_time` " +
            "FROM `pack_case_stock_log` WHERE `pack_case_number` = #{packCaseNumber}")
    List<PackCaseStockLog> findByPackCaseNumber(@Param("packCaseNumber") String packCaseNumber);

}
