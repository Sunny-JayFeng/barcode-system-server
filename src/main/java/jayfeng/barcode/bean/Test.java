package jayfeng.barcode.bean;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JayFeng
 * @date 2021/10/27
 */
@Data
public class Test {

    public Integer id;

    public LocalDateTime createTime;

    public LocalDateTime updateTime;

    public Integer value;

}
