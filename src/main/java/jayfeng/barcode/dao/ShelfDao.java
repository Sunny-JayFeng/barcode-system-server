package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.Shelf;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 货架数据持久层
 * @author JayFeng
 * @date 2021/10/20
 */
@Repository
public interface ShelfDao extends BaseMapper<Shelf> {

    /**
     * 根据货架编码判断货架是否已存在
     * @param shelfCode 货架编码
     * @return 返回
     */
    @Select("SELECT `id` FROM `shelf` WHERE `shelf_code` = #{shelfCode}")
    Integer findIdByShelfCode(@Param("shelfCode") String shelfCode);

    /**
     * 根据仓库编码，查询最新的货架编号
     * @param wareCode 仓库编码
     * @return 返回
     */
    @Select("SELECT `shelf_number` FROM `shelf` WHERE `ware_code` = #{wareCode} ORDER BY `shelf_number`")
    Integer findShelfNumberByWareCode(@Param("wareCode") String wareCode);
}
