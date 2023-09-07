package org.sunso.keypoint.trace.log.feign.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunso.keypoint.trace.log.feign.interceptor.FeignTraceLogInterceptor;

@Configuration
public class TraceLogFeignAutoConfiguration {

    @Bean
    public FeignTraceLogInterceptor feignTraceLogInterceptor() {
        return new FeignTraceLogInterceptor();
    }
}
