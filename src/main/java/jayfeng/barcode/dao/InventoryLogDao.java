package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.InventoryLog;
import org.springframework.stereotype.Repository;

/**
 * 盘点日志数据持久层
 * @author JayFeng
 * @date 2021/11/2
 */
@Repository
public interface InventoryLogDao extends BaseMapper<InventoryLog> {
}
