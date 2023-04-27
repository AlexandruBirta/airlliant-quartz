package ro.unibuc.fmi.airlliantquartz.configuration;


import com.fasterxml.uuid.Generators;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;


@Slf4j
@Component
@WebFilter(urlPatterns = {"/*"}, description = "Correlation-Id Filter")
public class CorrelationIdFilter implements Filter {

    private final BuildProperties buildProperties;

    @Autowired
    public CorrelationIdFilter(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        String applicationName = buildProperties.getArtifact() + "-" + buildProperties.getVersion();

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String requestCorrelationId = req.getHeader("Correlation-Id");

        res.addHeader("Origin-Application-Name", applicationName);

        try {

            MDC.put("Application-Name", applicationName);

            if (requestCorrelationId == null) {

                UUID uuid = Generators.timeBasedGenerator().generate();
                String correlationId = uuid.toString().replace("-", "");
                res.addHeader("Correlation-Id", correlationId);
                MDC.put("Correlation-Id", correlationId);

            } else {

                res.addHeader("Correlation-Id", requestCorrelationId);
                MDC.put("Correlation-Id", requestCorrelationId);

            }

            chain.doFilter(req, res);

        } finally {
            MDC.clear();
        }

    }

}