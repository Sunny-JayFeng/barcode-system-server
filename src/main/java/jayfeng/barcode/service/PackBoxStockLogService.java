package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.PackBoxStockLog;
import jayfeng.barcode.bean.Stock;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 袋库存拼盒日志数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Service
public interface PackBoxStockLogService {

    /**
     * 分页查询袋库存拼盒日志数据
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return
     */
    ResponseData findPackBoxStockLogPage(Map<String, String> requestParams, Page<PackBoxStockLog> page);

    /**
     * 通过 stockId 查询袋库存拼盒日志数据
     * @param stockIdList stockId 集合
     * @return 返回数据
     */
    List<PackBoxStockLog> findByStockIdList(Set<Integer> stockIdList);

    /**
     * 库存对象转库存拼盒日志对象
     * @param stock 库存对象
     * @param packBoxNumber 拼盒编码
     * @return 返回库存拼盒日志对象
     */
    PackBoxStockLog stockToPackBoxStockLog(Stock stock, String packBoxNumber);

    /**
     * 根据拼盒编号查询拼盒日志数据
     * @param packBoxNumber 拼盒编号
     * @return 返回
     */
    ResponseData findByPackBoxNumber(String packBoxNumber);

}
