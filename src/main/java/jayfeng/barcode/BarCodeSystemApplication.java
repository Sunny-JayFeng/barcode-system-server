package jayfeng.barcode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
@MapperScan(basePackages = "jayfeng.barcode.dao")
public class BarCodeSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarCodeSystemApplication.class, args);
    }

}
