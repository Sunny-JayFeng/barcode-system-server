package jayfeng.barcode.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理查询条件
 * @author JayFeng
 * @date 2021/10/13
 */
@Component
public class QueryConditionHandler {

    public static final String EQUAL = "eq";
    public static final String LIKE = "like";

    private String humpToUnderLine(String str) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            String target = matcher.group();
            str = str.replaceAll(target, "_" + target.toLowerCase());
        }
        return str;
    }

    /**
     * 处理查询条件
     * @param queryWrapper 查询页
     * @param queryTypeMap 参数查询类型
     * @param requestParams 查询参数值
     * @param <T> 泛型
     */
    public <T> void handleQueryCondition(QueryWrapper<T> queryWrapper, Map<String, String> queryTypeMap, Map<String, String> requestParams) {
        for (String key : queryTypeMap.keySet()) {
            String queryType = queryTypeMap.get(key); // 模糊查询还是精确匹配查询
            String queryValue = requestParams.get(key); // 查询值
            if (ObjectUtils.isEmpty(queryValue)) continue;
            // 精确匹配查询
            if (EQUAL.equals(queryType)) queryWrapper.eq(humpToUnderLine(key), queryValue);
            // 模糊查询  为了走索引，不允许以 % 开头进行模糊查询
            else if(LIKE.equals(queryType)) queryWrapper.likeRight(humpToUnderLine(key), queryValue);
        }
    }

    /**
     * 处理精确匹配的查询条件
     * @param queryWrapper 查询页
     * @param queryParams 查询参数
     * @param <T> 泛型
     */
    public <T> void handleEqualQueryCondition(QueryWrapper<T> queryWrapper, Map<String, String> queryParams) {
        for (String key : queryParams.keySet()) {
            String value = queryParams.get(key);
            if (!ObjectUtils.isEmpty(value)) queryWrapper.eq(humpToUnderLine(key), value);
        }
    }

    /**
     * 处理模糊查询的查询条件
     * @param queryWrapper 查询页
     * @param queryParams 查询参数
     * @param <T> 泛型
     */
    public <T> void handleLikeQueryCondition(QueryWrapper<T> queryWrapper, Map<String, String> queryParams) {
        for (String key : queryParams.keySet()) {
            String value = queryParams.get(key);
            if (!ObjectUtils.isEmpty(value)) queryWrapper.likeRight(humpToUnderLine(key), value);
        }
    }

    /**
     * 标准数量查询
     * @param queryWrapper 查询页
     * @param quantityParams 数量参数
     * @param <T> 泛型
     * @throws NumberFormatException 可能出现数字转换异常
     */
    public <T> void handleStandardQuantityQueryCondition(QueryWrapper<T> queryWrapper, Map<String, String> quantityParams) throws NumberFormatException {
        String boxStandardQuantity = quantityParams.get("boxStandardQuantity"); // 每盒标准数量
        String caseStandardQuantity = quantityParams.get("caseStandardQuantity"); // 每箱标准数量
        // 根据每盒标准数量查询
        if (!ObjectUtils.isEmpty(boxStandardQuantity)) queryWrapper.eq("box_standard_quantity", new BigDecimal(boxStandardQuantity));
        // 根据每箱标准数量查询
        if (!ObjectUtils.isEmpty(caseStandardQuantity)) queryWrapper.eq("case_standard_quantity", new BigDecimal(caseStandardQuantity));
    }

    /**
     * Byte数据作为查询条件
     * @param queryWrapper 查询页
     * @param statusParams 多个状态参数
     * @param <T> 泛型
     * @throws NumberFormatException 可能出现数字转换异常
     */
    public <T> void handleByteNumberQueryCondition(QueryWrapper<T> queryWrapper, Map<String, String> statusParams) throws NumberFormatException {
        for (String key : statusParams.keySet()) {
            String status = statusParams.get(key);
            if (!ObjectUtils.isEmpty(status)) queryWrapper.eq(humpToUnderLine(key), Byte.parseByte(status));
        }
    }

    /**
     * int数量作为查询条件
     * @param queryWrapper 查询页
     * @param quantityParams 多个数量参数
     * @param <T> 泛型
     * @throws NumberFormatException 可能出现数字转换异常
     */
    public <T> void handleQuantityQueryCondition(QueryWrapper<T> queryWrapper, Map<String, String> quantityParams) throws NumberFormatException {
        for (String key : quantityParams.keySet()) {
            String quantity = quantityParams.get(key);
            if (!ObjectUtils.isEmpty(quantity)) queryWrapper.eq(humpToUnderLine(key), Integer.parseInt(key));
        }
    }

}
