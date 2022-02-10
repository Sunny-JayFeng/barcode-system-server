package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 产品信息持久层
 * @author JayFeng
 * @date 2021/10/13
 */
@Repository
public interface ProductDao extends BaseMapper<Product> {

    /**
     * 根据料号查盒标准数量
     * @param proMaterialNumber 料号
     * @return 返回盒标准数量
     */
    @Select("SELECT `box_standard_quantity` FROM `product` WHERE `pro_material_number` = #{proMaterialNumber}")
    Integer findBoxStandardQuantity(@Param("proMaterialNumber") String proMaterialNumber);

    /**
     * 根据料号查箱标准数量
     * @param proMaterialNumber 料号
     * @return 返回箱标准数量
     */
    @Select("SELECT `case_standard_quantity` FROM `product` WHERE `pro_material_number` = #{proMaterialNumber}")
    Integer findCaseStandardQuantity(@Param("proMaterialNumber") String proMaterialNumber);

    /**
     * 根据料号获取两个标准数量
     * @param proMaterialNumber 料号
     * @return 返回
     */
    @Select("SELECT `box_standard_quantity`, `case_standard_quantity` FROM `product` WHERE `pro_material_number` = #{proMaterialNumber}")
    Product findStandardQuantity(@Param("proMaterialNumber") String proMaterialNumber);
}
