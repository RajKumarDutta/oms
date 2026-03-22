package com.rdutta.orderservice.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String id = Optional.ofNullable(req.getHeader(HEADER))
                .orElse(UUID.randomUUID().toString());

        MDC.put("correlationId", id);
        res.setHeader(HEADER, id);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}