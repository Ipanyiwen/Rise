package connector;

import javax.servlet.http.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class Response extends ResponseBase {

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
        StringBuilder res = new StringBuilder();
        String msg = status + " " + getStatusMessage(status);
        msg =  "<html>" +
                "<head><title>"+msg+"</title></head>" +
                "<body bgcolor=\"white\">" +
                "<center><h1>"+msg+"</h1></center>" +
                "<hr><center>Rise/1.0.0</center>" +
                "</body>" +
                "</html>";


        res.append(protocol).append(" ").append(status).append(" ").append(getStatusMessage(status)).append("\r\n");
        headers.put("content-type", "text/html; charset=utf-8");
        headers.put("content-length", msg.getBytes().length);
        for (Object key : headers.keySet()) {
            Object v = headers.get(key);
            res.append(key).append(": ").append(v).append("\r\n");
        }

        res.append("\r\n");
        res.append(msg);
        getWriter().write(res.toString());

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

}
