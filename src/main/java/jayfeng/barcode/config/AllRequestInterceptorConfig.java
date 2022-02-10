package jayfeng.barcode.config;

import jayfeng.barcode.interceptor.AllRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author JayFeng
 * @date 2021/10/24
 */
@Configuration
public class AllRequestInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private AllRequestInterceptor allRequestInterceptor;

    /**
     * 拦截器配置
     * @param registry 注册拦截器
     */
    public void addInterceptors(InterceptorRegistry registry ) {
//        registry.addInterceptor(allRequestInterceptor)
//                .addPathPatterns("/barcode/**")
//                .excludePathPatterns("/barcode/user/login/**",
//                                     "/barcode/user/registry/**");
    }

}
