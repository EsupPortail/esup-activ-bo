package org.esupportail.activbo.services.remote;

import static org.esupportail.activbo.Utils.removePrefixOrNull;
import static org.esupportail.activbo.Utils.removeSuffixOrNull;
import static org.esupportail.activbo.Utils.removeSuffix;
import static org.esupportail.activbo.Utils.mapSet;
import static org.esupportail.activbo.Utils.toSet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.activbo.Utils;
import org.esupportail.activbo.domain.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccountManagementImpl implements org.springframework.web.HttpRequestHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private DomainService domainService;
    
    public void afterPropertiesSet() throws Exception {
    }
    
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String response = handle(req);
            resp.setContentType("application/x-www-form-urlencoded");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(response);
        } catch (Exception e) {
            answerError(resp, e);
        }
    }   
    
    private void answerError(HttpServletResponse resp, Throwable e) {
        String exnName = e.getClass().getSimpleName();
        if (exnName.equals("InvalidParameterException") || exnName.equals("UserPermissionException")) {
                // known exceptions, no need to pollute logs with backtrace
                logger.error(""+e);
        } else {
                logger.error("", e);
        }
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try {
            var msg = "error=" + urlencode(exnName);
            if (!StringUtils.isEmpty(e.getMessage())) msg += "&message=" + urlencode(e.getMessage());
            resp.getWriter().write(msg);
        } catch (IOException e2) {
            logger.error("", e2);
        }        
    }

    private String handle(HttpServletRequest req) throws Exception {
        String action = getString(req, "action");
        switch (action) {
            case "validateAccount":
                return ok(domainService.validateAccount(getMap(req, "attr"), returnAttrs(req)));
            case "authentificateUser":
                return ok(domainService.authentificateUser(id(req), getString(req, "password"), returnAttrs(req)));
            case "authentificateUserWithCas":
                return ok(domainService.authentificateUserWithCas(id(req), getString(req, "proxyticket"), getString(req, "targetUrl"), returnAttrs(req)));
            case "authentificateUserWithCodeKey":
                return ok(domainService.authentificateUserWithCodeKey(id(req), getString(req, "accountCodeKey"), returnAttrs(req)));
            case "setPassword":
                domainService.setPassword(id(req), code(req), getString(req, "password"));
                return ok();
            case "updatePersonalInformations":
                domainService.updatePersonalInformations(id(req), code(req), getMultiMap(req, "attr"));
                return ok();
            case "sendCode":
                domainService.sendCode(id(req), getString(req, "channel"));
                return ok();
            case "verifyCode":
                domainService.verifyCode(id(req), code(req));
                return ok();
            case "changeLogin":
                domainService.changeLogin(id(req), code(req), getString(req, "newLogin"));
                return ok();
            case "validatePassword":
                return ok(domainService.validatePassword(id(req), getString(req, "password")));
            default:
                throw new InvalidParameterException("\"" + action + "\" is unknown action");
        }
    }

    private String id(HttpServletRequest req) {
        return getString(req, "id");
    }
    private String code(HttpServletRequest req) {
        return getString(req, "code");
    }
    private Set<String> returnAttrs(HttpServletRequest req) {
        // NB: ignoring ";base64" for now ; it will be added again based in attribute kind in LDAP
        return mapSet(getCommaStrings(req, "returnAttrs"), s -> removeSuffix(s, ";base64"));
    }
    
    private String ok() {
        return "";
    }
    private String ok(String resp) throws UnsupportedEncodingException {
        return "resp=" + (resp == null ? "" : urlencode(resp));
    }
    private String ok(Map<String, List<String>> map) throws UnsupportedEncodingException {
        return urlencode(map);
    }

    private String urlencode(Map<String, List<String>> map) throws UnsupportedEncodingException {
        var s = new StringBuilder();
        for (var entry: map.entrySet()) {
            for (var val: emptyStringIfEmptyList(entry.getValue())) {
                s.append(urlencode(entry.getKey()))
                    .append("=")
                    .append(urlencode(val))
                    .append("&");
            }
        }
        return s.toString();
    }

    private String urlencode(String s) throws UnsupportedEncodingException {
        return s != null ? URLEncoder.encode(s, "UTF-8") : null;
    }

    static List<String> getStrings(HttpServletRequest req, String name) {
        var vals = req.getParameterValues(name);
        if (vals == null) throw new InvalidParameterException("\"" + name + "\" parameter is mandatory");
        return Arrays.asList(vals);
    }
    static String getString(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val == null) throw new InvalidParameterException("\"" + name + "\" parameter is mandatory");
        return val;
    }
    static Set<String> getCommaStrings(HttpServletRequest req, String name) {
        String s = getString(req, name);
        return s.equals("") ? Collections.emptySet() : toSet(s.split(","));
    }
    static Map<String, String> getMap(HttpServletRequest req, String prefixName) {
        var r = new HashMap<String, String>();
        for (String paramName: Collections.list(req.getParameterNames())) {
            var relName = removePrefixOrNull(paramName, prefixName + ".");
            if (relName != null)
                r.put(relName, req.getParameter(paramName));
        }
        return r;
    }
    static Map<String, List<? extends Object>> getMultiMap(HttpServletRequest req, String prefixName) {
        var r = new HashMap<String, List<? extends Object>>();
        for (var paramName: Collections.list(req.getParameterNames())) {
            var relName = removePrefixOrNull(paramName, prefixName + ".");
            if (relName != null) {
                List<String> vals = emptyStringToEmptyList(Arrays.asList(req.getParameterValues(paramName)));
                var relNameBinary = removeSuffixOrNull(relName, ";base64");
                if (relNameBinary != null) {
                    r.put(relNameBinary, Utils.decodeBase64s(vals));
                } else {
                    r.put(relName, vals);
                }
            }
        }
        return r;
    }

    private List<String> emptyStringIfEmptyList(List<String> values) {
        return values.isEmpty() ? Collections.singletonList("") : values;
    }

    private static List<String> emptyStringToEmptyList(List<String> values) {
        return values.size() == 1 && "".equals(values.get(0)) ? Collections.emptyList() : values;
    }

}
