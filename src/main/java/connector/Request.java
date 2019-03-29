
package connector;


import util.RequestUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

public class Request implements HttpServletRequest {

    private Map<String, String> header = new HashMap();

    private SocketInputStream inputStream;

    private Locale defaultLocale = Locale.getDefault();

    private String requestedSessionId;

    private List<Cookie> cookies = new ArrayList<>();

    private List<Locale> locales = new ArrayList<>();

    private long length = 0;

    private String contenType;

    private int serverPort;

    private String serverName;

    private String queryString;

    private String method;

    private String protocol;

    private String requestURI;

    private String scheme;

    private Map<String, Object> parameters;

    private boolean parsed = false;

    public Request(SocketInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    public void addHeader(String key, String value) {
        this.header.put(key, value);
    }

    @Override
    public Cookie[] getCookies() {
        return (Cookie[]) cookies.toArray();
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        s = s.toLowerCase();
        if (header.containsKey(s)) {
            return header.get(s);
        }
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() { return null; }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    public void setRequestedSessionId(String sessionId) {
        this.requestedSessionId = sessionId;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {

        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0)
            port = 80; // Work around java.net.URL bug

        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if ((scheme.equals("http") && (port != 80))
                || (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());

        return (url);

    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return (int) length;
    }

    public void setContentLength(long l) {
        this.length = l;
    }

    @Override
    public long getContentLengthLong() {
        return length;
    }

    @Override
    public String getContentType() {
        return contenType;
    }

    public void setContentType(String type) {
        this.contenType = type;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getParameter(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return defaultLocale;
    }

    public void setLocale(Locale locale) {
        this.defaultLocale = locale;
    }

    public void addLocale(Locale locale) {
        this.locales.add(locale);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    protected void parseParameters() {

        if (parsed)
            return;

        Map<String, Object> results = parameters;
        if (results == null)
            results = new HashMap<>();

        String encoding = getCharacterEncoding();
        if (encoding == null)
            encoding = "ISO-8859-1";

        // Parse any parameters specified in the query string
        String queryString = getQueryString();
        try {
            RequestUtil.parseParameters(results, queryString, encoding);
        } catch (UnsupportedEncodingException e) {
            ;
        }

        // Parse any parameters specified in the input stream
        String contentType = getContentType();
        if (contentType == null)
            contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
        if ("POST".equals(getMethod()) && (getContentLength() > 0)
                && (this.inputStream == null)
                && "application/x-www-form-urlencoded".equals(contentType)) {

            try {
                int max = getContentLength();
                int len = 0;
                byte buf[] = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0 ) {
                        break;
                    }
                    len += next;
                }
                is.close();
                if (len < max) {
                    // FIX ME, mod_jk when sending an HTTP POST will sometimes
                    // have an actual content length received < content length.
                    // Checking for a read of -1 above prevents this code from
                    // going into an infinite loop.  But the bug must be in mod_jk.
                    // Log additional data when this occurs to help debug mod_jk
                    StringBuffer msg = new StringBuffer();
                    msg.append("HttpRequestBase.parseParameters content length mismatch\n");
                    msg.append("  URL: ");
                    msg.append(getRequestURL());
                    msg.append(" Content Length: ");
                    msg.append(max);
                    msg.append(" Read: ");
                    msg.append(len);
                    msg.append("\n  Bytes Read: ");
                    if ( len > 0 ) {
                        msg.append(new String(buf,0,len));
                    }
//                    log(msg.toString());
                    throw new RuntimeException("httpRequestBase.contentLengthMismatch");
                }
                RequestUtil.parseParameters(results, buf, encoding);
            } catch (UnsupportedEncodingException ue) {
                ;
            } catch (IOException e) {
                throw new RuntimeException("httpRequestBase.contentReadFail" + e.getMessage());
            }
        }

        parsed = true;
        parameters = results;

    }


}
