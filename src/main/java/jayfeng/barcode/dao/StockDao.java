package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存数据持久层
 * @author JayFeng
 * @date 2021/10/21
 */
@Repository
public interface StockDao extends BaseMapper<Stock> {

    /**
     * 根据货架编码和仓库编码查询库存
     * @param shelfCode 货架编码
     * @return 返回
     */
    @Select("SELECT `id`, `pro_material_number`, `pro_model`, `lot_number`, `amount`," +
            "`type`, `status`, `shipment_number`, `pack_box_number`, `pack_case_number`," +
            "`ware_code`, `ware_name`, `shelf_code`, `in_time`, `out_time`" +
            "WHERE `shelf_code` = #{shelfCode}")
    List<Stock> findByShelfCode(@Param("shelfCode") String shelfCode);

    /**
     * 根据出货单号查询库存数据
     * @param goodsOrderNumber 出货单号
     * @return 返回
     */
    @Select("SELECT `id`, `pro_material_number`, `pro_model`, `lot_number`, `amount`," +
            "`type`, `status`, `goods_order_number`, `is_pack_box`, `pack_box_number`, " +
            "`is_pack_case`, `pack_case_number`," +
            "`ware_code`, `ware_name`, `shelf_code`, `in_time` " +
            "WHERE `goods_order_number` = #{goodsOrderNumber}")
    List<Stock> findByGoodsOrderNumber(@Param("goodsOrderNumber") String goodsOrderNumber);

    /**
     * 更具货架编码修改货架上库存货物的仓库编码
     * @param shelfCode 货架编码
     * @param newWareCode 新仓库编码
     * @param updateTime 修改时间
     */
    @Update("UPDATE FROM `stock` SET `ware_code` = #{newWareCode}, `update_time` = #{updateTime} " +
            "WHERE `shelf_code` = #{shelfCode}")
    void updateStockWareCode(@Param("shelfCode") String shelfCode, @Param("newWareCode") String newWareCode, @Param("updateTime")LocalDateTime updateTime);

    /**
     * 根据序列号查询库存
     * @param serialNumber 序列号
     * @return 库存
     */
    @Select("SELECT * FROM `stock` WHERE `serail_number` = #{serialNumber}")
    Stock findBySerialNumber(@Param("serialNumber") Long serialNumber);



}
