package jayfeng.barcode.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jayfeng.barcode.bean.Customer;
import org.springframework.stereotype.Repository;

/**
 * 客户信息持久层
 * @author JayFeng
 * @date 2021/10/19
 */
@Repository
public interface CustomerDao extends BaseMapper<Customer> {
}
