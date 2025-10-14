package org.iebbuda.mozi.domain.account.external;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.iebbuda.mozi.domain.account.external"})
public class ExternalApiClientConfig {
    // RestTemplate은 RootConfig에서 정의됨
}
