package jayfeng.barcode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jayfeng.barcode.bean.Shelf;
import jayfeng.barcode.bean.Warehouse;
import jayfeng.barcode.response.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 仓库信息业务逻辑层
 * @author JayFeng
 * @date 2021/10/21
 */
@Service
public interface WarehouseService {

    /**
     * 分页查询仓库信息
     * @param requestParams 查询参数
     * @param page 分页参数
     * @return 返回
     */
    ResponseData findWarehousePage(Map<String, String> requestParams, Page<Warehouse> page);

    /**
     * 查询所有仓库信息
     * @return 返回
     */
    ResponseData findWarehouse();

    /**
     * 添加仓库信息
     * @param wareHouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    ResponseData addWarehouse(Warehouse wareHouse, String password);

    /**
     * 修改仓库信息
     * @param wareHouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    ResponseData updateWarehouse(Warehouse wareHouse, String password);

    /**
     * 删除仓库信息
     * @param wareHouse 待添加的仓库信息
     * @param password 当前已登录的用户的密码
     * @return 返回
     */
    ResponseData deleteWarehouse(Warehouse wareHouse, String password);

    /**
     * 更新仓库信息 -- 主要是：增删货架个数、
     * 修改仓库管理员(比较少) 如果修改仓库管理员，那么仓库二维码需要修改，重贴标签
     * @param wareHouse 待更新仓库
     * @param userName 操作人
     */
    void updateWarehouseMessage(Warehouse wareHouse, String userName);

    /**
     * 以非事务方式更新仓库信息
     * @param warehouse 仓库信息
     */
    void notSupportedUpdateWarehouse(Warehouse warehouse);

    /**
     * 验证是否为良品仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    boolean isQualifiedProductsWarehouse(String wareCode);

    /**
     * 验证是否为不良品仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    boolean isUnQualifiedProductsWarehouse(String wareCode);

    /**
     * 验证是否为检测仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    boolean isInspectionWarehouse(String wareCode);

    /**
     * 验证是否为出货仓
     * @param wareCode 仓库编码
     * @return 返回
     */
    boolean isShipmentWarehouse(String wareCode);

    /**
     * 两个仓库是否为同类型的仓库
     * @param wareCode1 仓库编码1
     * @param wareCode2 仓库编码2
     * @return 返回
     */
    boolean isSameTypeWarehouse(String wareCode1, String wareCode2);

    /**
     * 打印仓库标签
     * @param warehouse 仓库信息
     * @return 返回
     */
    ResponseData printWarehouseLabel(Warehouse warehouse);
}
