package com.partner.backend.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logs every inbound HTTP request so we can confirm mobile devices are
 * actually reaching the backend (vs. being blocked client-side by Android
 * cleartext or iOS ATS policy before the TCP connection is ever made).
 *
 * Output example:
 *   [HTTP] POST /api/patient/auth/otp/email/send  from 192.168.100.5  → 200  (312 ms)
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  req;
        HttpServletResponse httpRes  = (HttpServletResponse) res;

        String method    = httpReq.getMethod();
        String uri       = httpReq.getRequestURI();
        String clientIp  = resolveClientIp(httpReq);
        long   startMs   = System.currentTimeMillis();

        try {
            chain.doFilter(req, res);
        } finally {
            long elapsed = System.currentTimeMillis() - startMs;
            int  status  = httpRes.getStatus();
            if (status >= 500) {
                log.error("[HTTP] {} {}  from {}  → {}  ({} ms)", method, uri, clientIp, status, elapsed);
            } else if (status >= 400) {
                log.warn ("[HTTP] {} {}  from {}  → {}  ({} ms)", method, uri, clientIp, status, elapsed);
            } else {
                log.info ("[HTTP] {} {}  from {}  → {}  ({} ms)", method, uri, clientIp, status, elapsed);
            }
        }
    }

    private static String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
