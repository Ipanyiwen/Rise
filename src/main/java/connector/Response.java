package connector;

import javax.servlet.http.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


public class Response extends ResponseBase {

    protected String message = getStatusMessage(HttpServletResponse.SC_OK);

    protected String characterEncoding = null;

    protected int status = HttpServletResponse.SC_OK;

    protected final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);



    @Override
    public void addCookie(Cookie cookie) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        synchronized (cookies) {
            cookies.add(cookie);
        }
    }

    @Override
    public boolean containsHeader(String s) {
        return headers.containsKey(s);
    }

    private boolean isEncodeable(String location) {

        if (location == null)
            return (false);

        // Is this an intra-document reference?
        if (location.startsWith("#"))
            return (false);

        // Are we in a valid session that is not using cookies?
        HttpServletRequest hreq = request;
        HttpSession session = hreq.getSession(false);
        if (session == null)
            return (false);
        if (hreq.isRequestedSessionIdFromCookie())
            return (false);

        // Is this a valid absolute URL?
        URL url = null;
        try {
            url = new URL(location);
        } catch (MalformedURLException e) {
            return (false);
        }

        // Does this URL match down to (and including) the context path?
        if (!hreq.getScheme().equalsIgnoreCase(url.getProtocol()))
            return (false);
        if (!hreq.getServerName().equalsIgnoreCase(url.getHost()))
            return (false);
        int serverPort = hreq.getServerPort();
        if (serverPort == -1) {
            if ("https".equals(hreq.getScheme()))
                serverPort = 443;
            else
                serverPort = 80;
        }
        int urlPort = url.getPort();
        if (urlPort == -1) {
            if ("https".equals(url.getProtocol()))
                urlPort = 443;
            else
                urlPort = 80;
        }
        if (serverPort != urlPort)
            return (false);

        String file = url.getFile();
        if ((file == null))
            return (false);
        if (file.indexOf(";jsessionid=" + session.getId()) >= 0)
            return (false);

        // This URL belongs to our web application, so it is encodeable
        return (true);

    }

    private String toEncoded(String url, String sessionId) {

        if ((url == null) || (sessionId == null))
            return (url);

        String path = url;
        String query = "";
        String anchor = "";
        int question = url.indexOf('?');
        if (question >= 0) {
            path = url.substring(0, question);
            query = url.substring(question);
        }
        int pound = path.indexOf('#');
        if (pound >= 0) {
            anchor = path.substring(pound);
            path = path.substring(0, pound);
        }
        StringBuffer sb = new StringBuffer(path);
        if( sb.length() > 0 ) { // jsessionid can't be first.
            sb.append(";jsessionid=");
            sb.append(sessionId);
        }
        sb.append(anchor);
        sb.append(query);
        return (sb.toString());

    }

    @Override
    public String encodeURL(String url) {
        if (isEncodeable(toAbsolute(url))) {
            HttpServletRequest hreq = request;
            return (toEncoded(url, hreq.getSession().getId()));
        } else
            return (url);
    }

    @Override
    public String encodeRedirectURL(String url) {

        if (isEncodeable(toAbsolute(url))) {
            HttpServletRequest hreq = request;
            return (toEncoded(url, hreq.getSession().getId()));
        } else
            return (url);
    }

    @Override
    public String encodeUrl(String url) {
        return (encodeURL(url));
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return (encodeRedirectURL(url));
    }

    @Override
    public void sendError(int status, String message) throws IOException {
        if (isCommitted())
            throw new IllegalStateException("httpResponseBase.sendError.ise");

        if (included)
            return;     // Ignore any call from an included servlet

        setError();

        // Record the status code and message.
        this.status = status;
        this.message = message;

        // Clear any data content that has been buffered
        resetBuffer();

        // Cause the response to be finished (from the application perspective)
        setSuspended(true);
    }

    @Override
    public void sendError(int status) throws IOException {
        sendError(status, getStatusMessage(status));
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if (isCommitted())
            throw new IllegalStateException("httpResponseBase.sendRedirect.ise");

        if (included)
            return;     // Ignore any call from an included servlet

        // Clear any data content that has been buffered
        resetBuffer();

        // Generate a temporary redirect to the specified location
        try {
            String absolute = toAbsolute(location);
            setStatus(SC_MOVED_TEMPORARILY);
            setHeader("Location", absolute);
        } catch (IllegalArgumentException e) {
            setStatus(SC_NOT_FOUND);
        }

        // Cause the response to be finished (from the application perspective)
        setSuspended(true);
    }

    private String toAbsolute(String location) {

        if (location == null)
            return (location);

        // Construct a new absolute URL if possible (cribbed from
        // the DefaultErrorPage servlet)
        URL url = null;
        try {
            url = new URL(location);
        } catch (MalformedURLException e1) {
            HttpServletRequest hreq = request;
            String requrl = HttpUtils.getRequestURL(hreq).toString();
            try {
                url = new URL(new URL(requrl), location);
            } catch (MalformedURLException e2) {
                throw new IllegalArgumentException(location);
            }
        }
        return (url.toExternalForm());
    }

    @Override
    public void setDateHeader(String name, long value) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        setHeader(name, format.format(new Date(value)));
    }

    @Override
    public void addDateHeader(String name, long value) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        addHeader(name, format.format(new Date(value)));
    }

    @Override
    public void setHeader(String name, String value) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        ArrayList values = new ArrayList();
        values.add(value);
        synchronized (headers) {
            headers.put(name, values);
        }

        String match = name.toLowerCase();
        if (match.equals("content-length")) {
            int contentLength = -1;
            try {
                contentLength = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                ;
            }
            if (contentLength >= 0)
                setContentLength(contentLength);
        } else if (match.equals("content-type")) {
            setContentType(value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if (values == null) {
                values = new ArrayList();
                headers.put(name, values);
            }
            values.add(value);
        }
    }

    @Override
    public void setIntHeader(String name, int value) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        setHeader(name, "" + value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        addHeader(name, "" + value);
    }

    @Override
    public void setStatus(int status) {
        setStatus(status, getStatusMessage(status));
    }

    @Override
    public void setStatus(int status, String message) {
        if (included)
            return;     // Ignore any call from an included servlet

        this.status = status;
        this.message = message;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String name) {
        ArrayList values = null;
        synchronized (headers) {
            values = (ArrayList) headers.get(name);
        }
        if (values != null)
            return ((String) values.get(0));
        else
            return (null);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        ArrayList values = null;
        synchronized (headers) {
            values = (ArrayList) headers.get(name);
        }
        if (values == null)
            return (new ArrayList<>());

        return values;
    }

    @Override
    public Collection<String> getHeaderNames() {
        synchronized (headers) {
            return headers.keySet();
        }
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        if (isCommitted()) {
            return;
        }
        if (characterEncoding == null) {
            return;
        }

        this.characterEncoding = characterEncoding;

    }

    @Override
    public void setContentLengthLong(long l) {
        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        this.contentLength = l;
    }

    protected String getStatusMessage(int status) {

        switch (status) {
            case SC_OK:
                return ("OK");
            case SC_ACCEPTED:
                return ("Accepted");
            case SC_BAD_GATEWAY:
                return ("Bad Gateway");
            case SC_BAD_REQUEST:
                return ("Bad Request");
            case SC_CONFLICT:
                return ("Conflict");
            case SC_CONTINUE:
                return ("Continue");
            case SC_CREATED:
                return ("Created");
            case SC_EXPECTATION_FAILED:
                return ("Expectation Failed");
            case SC_FORBIDDEN:
                return ("Forbidden");
            case SC_GATEWAY_TIMEOUT:
                return ("Gateway Timeout");
            case SC_GONE:
                return ("Gone");
            case SC_HTTP_VERSION_NOT_SUPPORTED:
                return ("HTTP Version Not Supported");
            case SC_INTERNAL_SERVER_ERROR:
                return ("Internal Server Error");
            case SC_LENGTH_REQUIRED:
                return ("Length Required");
            case SC_METHOD_NOT_ALLOWED:
                return ("Method Not Allowed");
            case SC_MOVED_PERMANENTLY:
                return ("Moved Permanently");
            case SC_MOVED_TEMPORARILY:
                return ("Moved Temporarily");
            case SC_MULTIPLE_CHOICES:
                return ("Multiple Choices");
            case SC_NO_CONTENT:
                return ("No Content");
            case SC_NON_AUTHORITATIVE_INFORMATION:
                return ("Non-Authoritative Information");
            case SC_NOT_ACCEPTABLE:
                return ("Not Acceptable");
            case SC_NOT_FOUND:
                return ("Not Found");
            case SC_NOT_IMPLEMENTED:
                return ("Not Implemented");
            case SC_NOT_MODIFIED:
                return ("Not Modified");
            case SC_PARTIAL_CONTENT:
                return ("Partial Content");
            case SC_PAYMENT_REQUIRED:
                return ("Payment Required");
            case SC_PRECONDITION_FAILED:
                return ("Precondition Failed");
            case SC_PROXY_AUTHENTICATION_REQUIRED:
                return ("Proxy Authentication Required");
            case SC_REQUEST_ENTITY_TOO_LARGE:
                return ("Request Entity Too Large");
            case SC_REQUEST_TIMEOUT:
                return ("Request Timeout");
            case SC_REQUEST_URI_TOO_LONG:
                return ("Request URI Too Long");
            case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
                return ("Requested Range Not Satisfiable");
            case SC_RESET_CONTENT:
                return ("Reset Content");
            case SC_SEE_OTHER:
                return ("See Other");
            case SC_SERVICE_UNAVAILABLE:
                return ("Service Unavailable");
            case SC_SWITCHING_PROTOCOLS:
                return ("Switching Protocols");
            case SC_UNAUTHORIZED:
                return ("Unauthorized");
            case SC_UNSUPPORTED_MEDIA_TYPE:
                return ("Unsupported Media Type");
            case SC_USE_PROXY:
                return ("Use Proxy");
            case 207:       // WebDAV
                return ("Multi-Status");
            case 422:       // WebDAV
                return ("Unprocessable Entity");
            case 423:       // WebDAV
                return ("Locked");
            case 507:       // WebDAV
                return ("Insufficient Storage");
            default:
                return ("HTTP Response Status " + status);
        }

    }
}
