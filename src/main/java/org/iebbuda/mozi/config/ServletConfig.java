package org.iebbuda.mozi.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.*;

@EnableWebMvc
@ComponentScan(basePackages = {
        "org.iebbuda.mozi.controller",
        "org.iebbuda.mozi.exception",
        "org.iebbuda.mozi.domain.user.controller",
        "org.iebbuda.mozi.domain.policy",
        "org.iebbuda.mozi.domain.product.controller",
        "org.iebbuda.mozi.domain.profile.controller",
        "org.iebbuda.mozi.domain.goal.controller",
        "org.iebbuda.mozi.domain.account.controller",
        "org.iebbuda.mozi.domain.scrap",
        "org.iebbuda.mozi.domain.security.controller",
        "org.iebbuda.mozi.domain.recommend.controller"

})    // Spring MVC용 컴포넌트 등록을 위한 스캔 패키지
public class ServletConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry){
        registry.addViewController("/")
                .setViewName("forward:/resources/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/resources/**") // url이 /resources/로 시작하는 모든 경로
                .addResourceLocations("/resources/"); // webapp/resources/경로로 매핑

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("/resources/assets/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("/resources/images/");

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("/resources/favicon.ico");

        // Swagger UI 리소스를 위한 핸들러 설정
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        // Swagger WebJar 리소스 설정
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        // Swagger 리소스 설정
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/v2/api-docs")
                .addResourceLocations("classpath:/META-INF/resources/");

    }



}