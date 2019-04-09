package connector;

import core.Mapper;
import core.Service;
import util.RequestUtil;
import util.StringParser;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;


public class SocketProcessor implements Processor, Runnable {

    private static final String match =
            ";" + Constants.SESSION_PARAMETER_NAME + "=";

    private boolean keepAlive = false;

    private boolean http11 = true;

    private boolean sendAck = false;

    private static boolean ok = true;

    private static final byte[] ack = (new String("HTTP/1.1 100 Continue\r\n\r\n")).getBytes();

    private static final byte[] NotFound404 = (new String("HTTP/1.1 404 Not Found\r\n" +
            "Connection: keep-alive\r\n" +
            "Content-Encoding: utf-8\r\n" +
            "Content-Type: text/html; charset=utf8,gbk\r\n" +
            "Server: Rise/1.0.0 \r\n\r\n" +
            "<html>" +
            "<head><title>404 Not Found</title></head>" +
            "<body bgcolor=\"white\">" +
            "<center><h1>404 Not Found</h1></center>" +
            "<hr><center>Rise/1.0.0</center>" +
            "</body>" +
            "</html>")).getBytes();

    private static final String OKMSG = new String("HTTP/1.1 200 OK\r\n" +
            "Connection: keep-alive\r\n" +
            "Content-Encoding: utf-8\r\n" +
            "Content-Type: text/html; charset=utf8,gbk\r\n" +
            "Server: Rise/1.0.0 \r\n\r\n");

    private Socket socket;
    private Request request;
    private Response response;
    private Connector connector;
    private HttpRequestLine requestLine = new HttpRequestLine();

    private StringParser parser = new StringParser();

    public SocketProcessor(Socket socket, Connector connector) {
        if (socket == null || connector == null) {
            throw new NullPointerException();
        }
        this.socket = socket;
        this.connector = connector;
    }

    @Override
    public void run() {
        if (socket != null) {
            try {
                process();
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process() throws ServletException, IOException {
        try {
            parseSocket();
            if (ok) {
                Service service = Mapper.getService(request.getRequestURI());
                if (service == null) {
                    response.getOutputStream().write(NotFound404);
                    response.finishResponse();
                } else {
                    service.invoke(request, response);
                }
                socket.close();
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            throw e;
        }

    }

    private void parseSocket() throws IOException, ServletException {
        try {

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            SocketInputStream in = new SocketInputStream(inputStream, Constants.bufferSize);
            request = new Request(in);
            response = new Response();
            response.setStream(outputStream);
            parseConnection();
            parseRequest(in, outputStream);
            parseHeaders(in);
            if (http11) {
                ackRequest(outputStream);
            }
        } catch (Exception e) {
            ok = false;
            throw e;
        }

    }

    private void parseConnection() {
        if (connector.getProxyPort() != 0) {
            request.setServerPort(connector.getProxyPort());
        }
        else {
            request.setServerPort(connector.getPort());
        }
    }

    private void parseRequest(SocketInputStream input, OutputStream outputStream) throws IOException, ServletException {
        // Parse the incoming request line
        input.readRequestLine(requestLine);
        String method = new String(requestLine.method, 0, requestLine.methodEnd);
        String uri = null;
        String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

        if (protocol.length() == 0) {
            protocol = "HTTP/0.9";
        }

        if ( protocol.equals("HTTP/1.1") ) {
            http11 = true;
            sendAck = false;
        } else {
            http11 = false;
            sendAck = false;
            keepAlive = false;
        }

        if (method.length() < 1) {
            throw new ServletException("httpProcessor.parseRequest.method");
        } else if (requestLine.uriEnd < 1) {
            throw new ServletException("httpProcessor.parseRequest.uri");
        }

        // Parse any query parameters out of the request URI
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            request.setQueryString(new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1));
            uri = new String(requestLine.uri, 0, question);
        } else {
            request.setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }

        // Checking for an absolute URI (with the HTTP protocol)
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            // Parsing out protocol and host name
            if (pos != -1) {
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    uri = uri.substring(pos);
                }
            }
        }

        // Parse any requested session ID out of the request URI
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            uri = uri.substring(0, semicolon) + rest;
        } else {
            request.setRequestedSessionId(null);
        }

        // Normalize URI (using String operations at the moment)
        String normalizedUri = normalize(uri);

        // Set the corresponding request properties
        request.setMethod(method);
        request.setProtocol(protocol);
        if (normalizedUri != null) {
            request.setRequestURI(normalizedUri);
        } else {
            request.setRequestURI(uri);
        }
//        request.setSecure(connector.getSecure());
        request.setScheme(connector.getScheme());

        if (normalizedUri == null) {
            throw new ServletException("Invalid URI: " + uri + "'");
        }
    }

    private void parseHeaders(SocketInputStream input)
            throws IOException, ServletException {

        while (true) {

            HttpHeader header = new HttpHeader();

            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("httpProcessor.parseHeaders.colon");
                }
            }

            String value = new String(header.value, 0, header.valueEnd);
            String key = new String(header.name).substring(0, header.nameEnd);
            request.addHeader(key, value);
            if (header.equals(DefaultHeaders.AUTHORIZATION_NAME)) {
//                request.setAuthorization(value); TODO
            } else if (header.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)) {
                parseAcceptLanguage(value);
            } else if (header.equals(DefaultHeaders.COOKIE_NAME)) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals
                            (Constants.SESSION_COOKIE_NAME)) {
                        // Override anything requested in the URL
                        if (!request.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie

                            request.setRequestedSessionId
                                    (cookies[i].getValue());

                        }
                    }
                    request.addCookie(cookies[i]);
                }
            } else if (header.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException("httpProcessor.parseHeaders.contentLength");
                }
                request.setContentLength(n);
            } else if (header.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
                request.setContentType(value);
            } else if (header.equals(DefaultHeaders.HOST_NAME)) {
                int n = value.indexOf(':');
                if (n < 0) {
                    if (connector.getScheme().equals("http")) {
                        request.setServerPort(80);

                    } else if (connector.getScheme().equals("https")) {
                        request.setServerPort(443);
                    }
                    if (connector.getProxyName() != null)
                        request.setServerName(connector.getProxyName());
                    else
                        request.setServerName(value);
                } else {
                    if (connector.getProxyName() != null)
                        request.setServerName(connector.getProxyName());
                    else
                        request.setServerName(value.substring(0, n).trim());
                    if (connector.getProxyPort() != 0)
                        request.setServerPort(connector.getProxyPort());
                    else {
                        int port = 80;
                        try {
                            port =
                                    Integer.parseInt(value.substring(n+1).trim());
                        } catch (Exception e) {
                            throw new ServletException("httpProcessor.parseHeaders.portNumber");
                        }
                        request.setServerPort(port);
                    }
                }
            }

            else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (header.valueEquals
                        (DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
                    keepAlive = false;
                    response.setHeader("Connection", "close");
                }
                //request.setConnection(header);
                /*
                  if ("keep-alive".equalsIgnoreCase(value)) {
                  keepAlive = true;
                  }
                */
            } else if (header.equals(DefaultHeaders.EXPECT_NAME)) {
                if (header.valueEquals(DefaultHeaders.EXPECT_100_VALUE))
                    sendAck = true;
                else
                    throw new ServletException("httpProcessor.parseHeaders.unknownExpectation");
            } else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {
                //request.setTransferEncoding(header)
            }
        }

    }

    private void parseAcceptLanguage(String value) {

        // Store the accumulated languages that have been requested in
        // a local collection, sorted by the quality value (so we can
        // add Locales in descending order).  The values will be ArrayLists
        // containing the corresponding Locales to be added
        TreeMap locales = new TreeMap();

        // Preprocess the value to remove all whitespace
        int white = value.indexOf(' ');
        if (white < 0)
            white = value.indexOf('\t');
        if (white >= 0) {
            StringBuffer sb = new StringBuffer();
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char ch = value.charAt(i);
                if ((ch != ' ') && (ch != '\t'))
                    sb.append(ch);
            }
            value = sb.toString();
        }

        // Process each comma-delimited language specification
        parser.setString(value);        // ASSERT: parser is available to us
        int length = parser.getLength();
        while (true) {

            // Extract the next comma-delimited entry
            int start = parser.getIndex();
            if (start >= length)
                break;
            int end = parser.findChar(',');
            String entry = parser.extract(start, end).trim();
            parser.advance();   // For the following entry

            // Extract the quality factor for this entry
            double quality = 1.0;
            int semi = entry.indexOf(";q=");
            if (semi >= 0) {
                try {
                    quality = Double.parseDouble(entry.substring(semi + 3));
                } catch (NumberFormatException e) {
                    quality = 0.0;
                }
                entry = entry.substring(0, semi);
            }

            // Skip entries we are not going to keep track of
            if (quality < 0.00005)
                continue;       // Zero (or effectively zero) quality factors
            if ("*".equals(entry))
                continue;       // FIXME - "*" entries are not handled

            // Extract the language and country for this entry
            String language = null;
            String country = null;
            String variant = null;
            int dash = entry.indexOf('-');
            if (dash < 0) {
                language = entry;
                country = "";
                variant = "";
            } else {
                language = entry.substring(0, dash);
                country = entry.substring(dash + 1);
                int vDash = country.indexOf('-');
                if (vDash > 0) {
                    String cTemp = country.substring(0, vDash);
                    variant = country.substring(vDash + 1);
                    country = cTemp;
                } else {
                    variant = "";
                }
            }

            // Add a new Locale to the list of Locales for this quality level
            Locale locale = new Locale(language, country, variant);
            Double key = new Double(-quality);  // Reverse the order
            ArrayList values = (ArrayList) locales.get(key);
            if (values == null) {
                values = new ArrayList();
                locales.put(key, values);
            }
            values.add(locale);

        }

        Iterator keys = locales.keySet().iterator();
        while (keys.hasNext()) {
            Double key = (Double) keys.next();
            ArrayList list = (ArrayList) locales.get(key);
            Iterator values = list.iterator();
            while (values.hasNext()) {
                Locale locale = (Locale) values.next();
                request.addLocale(locale);

            }
        }

    }

    protected String normalize(String path) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        // Normalize "/%7E" and "/%7e" at the beginning to "/~"
        if (normalized.startsWith("/%7E") ||
                normalized.startsWith("/%7e"))
            normalized = "/~" + normalized.substring(4);

        // Prevent encoding '%', '/', '.' and '\', which are special reserved
        // characters
        if ((normalized.indexOf("%25") >= 0)
                || (normalized.indexOf("%2F") >= 0)
                || (normalized.indexOf("%2E") >= 0)
                || (normalized.indexOf("%5C") >= 0)
                || (normalized.indexOf("%2f") >= 0)
                || (normalized.indexOf("%2e") >= 0)
                || (normalized.indexOf("%5c") >= 0)) {
            return null;
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                    normalized.substring(index + 3);
        }

        // Declare occurrences of "/..." (three or more dots) to be invalid
        // (on some Windows platforms this walks the directory tree!!!)
        if (normalized.indexOf("/...") >= 0)
            return (null);

        // Return the normalized path that we have completed
        return (normalized);

    }


    private void ackRequest(OutputStream output)
            throws IOException {
        if (sendAck)
            output.write(ack);
    }

}
