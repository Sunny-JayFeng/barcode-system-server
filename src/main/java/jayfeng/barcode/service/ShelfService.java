package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Shelf;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 货架数据业务逻辑层
 * @author JayFeng
 * @date 2021/10/20
 */
@Service
public interface ShelfService {

    /**
     * 分页查询货架信息
     * @param requestParams 请求参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findShelfPage(Map<String, String> requestParams, Page<Shelf> page);

    /**
     * 根据仓库编码，查询最新的货架编号
     * @param wareCode 仓库编码
     * @return 返回
     */
    ResponseData findShelfNumberByWareCode(String wareCode);

    /**
     * 添加货架信息
     * @param shelf 待添加的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    ResponseData addShelf(Shelf shelf, String password);

    /**
     *
     * @param shelf 待更新的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    ResponseData updateShelf(Shelf shelf, String password);

    /**
     * 更新货架信息
     * @param shelf 待更新的货架
     */
    void updateShelfMessage(Shelf shelf, String userName);

    /**
     * @param shelf 待删除的货架
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    ResponseData deleteShelf(Shelf shelf, String password);

    /**
     * 移货架  从一个仓库移动到另外一个仓库
     * @param shelf 待移动的仓库
     * @param targetWarehouse 目标仓库
     * @return 返回
     */
    ResponseData moveShelf(Shelf shelf, String targetWarehouse);

    /**
     * 打印货架标签
     * @param shelf 货架
     * @return 返回
     */
    ResponseData printShelfLabel(Shelf shelf);

}
