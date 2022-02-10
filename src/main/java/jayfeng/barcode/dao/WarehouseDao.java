package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.Warehouse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 仓库信息持久层
 * @author JayFeng
 * @date 2021/10/21
 */
@Repository
public interface WarehouseDao extends BaseMapper<Warehouse> {

    /**
     * 根据仓库编码查询仓库
     * @param wareCode 仓库编码
     * @return 返回
     */
    @Select("SELECT `id`, `ware_code`, `ware_name`, `manager`, `warehouse_qr_code`, " +
            "`create_time`, `update_time`" +
            "FROM `warehouse` WHERE `ware_code` = #{wareCode}")
    Warehouse findWareHouseByWareCode(@Param("wareCode") String wareCode);

    @Select("SELECT `id`, `ware_code`, `ware_name` FROM `warehouse` WHERE `id` = #{warehouseId}")
    Warehouse findById(@Param("warehouseId") Integer warehouseId);

}
