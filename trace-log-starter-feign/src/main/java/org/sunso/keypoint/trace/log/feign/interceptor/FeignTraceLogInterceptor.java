package org.sunso.keypoint.trace.log.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.sunso.keypoint.trace.log.common.utils.MDCTraceUtils;

import java.util.Enumeration;
import java.util.Objects;


@Slf4j
public class FeignTraceLogInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        log.info("FeignTraceLogInterceptor path[{}]" + requestTemplate.path());
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 传递请求相关header
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    // 跳过 content-length
                    if (Objects.equals("content-length", name)){
                        continue;
                    }
                    String value = request.getHeader(name);
                    requestTemplate.header(name, value);
                }
            }
        }
        // 传递日志追踪的traceId
        String traceId = MDCTraceUtils.getTraceId();
        log.info("FeignTraceLogInterceptor traceId[{}]", traceId);
        if (StringUtils.isNotBlank(traceId)) {
            requestTemplate.header(MDCTraceUtils.TRACE_ID_HEADER, traceId);
        }
    }
}
