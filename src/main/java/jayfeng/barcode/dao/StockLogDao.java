package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.StockLog;
import org.springframework.stereotype.Repository;

/**
 * 库存操作日志数据持久层
 * @author JayFeng
 * @date 2021/10/21
 */
@Repository
public interface StockLogDao extends BaseMapper<StockLog> {
}
