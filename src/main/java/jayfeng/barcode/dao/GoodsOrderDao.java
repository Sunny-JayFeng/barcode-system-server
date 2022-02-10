package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.GoodsOrder;
import org.springframework.stereotype.Repository;

/**
 * 出货单数据持久层
 * @author JayFeng
 * @date 2021/11/5
 */
@Repository
public interface GoodsOrderDao extends BaseMapper<GoodsOrder> {
}
