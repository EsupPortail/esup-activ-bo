package org.esupportail.activbo.web;
 
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.MDC;
 
/**
 * Filtre d'initialisation pour Log4J.
 * Exemple
 *   <init-param>
 *     <param-name>headers</param-name>
 *     <param-value>X-Forwarded-For</param-value>
 *   </init-param>
 * Permet le pattern %X{X-Forwarded-For} pour avoir ce header HTTP dans les logs
 */
public class Log4jFilterAddMDC implements Filter {
    private FilterConfig config;
    
    public void init(FilterConfig config) { this.config = config; }
    public void destroy() {}
 
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        for (String name : config.getInitParameter("headers").split(" ")) {
            String val = ((HttpServletRequest) request).getHeader(name);
            if (val != null) MDC.put(name, val);
        }
        chain.doFilter(request, response);
    } 
}
