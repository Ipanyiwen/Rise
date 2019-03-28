package connector;

import util.RequestUtil;
import util.StringParser;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;


public class SocketProcessor implements Processor, Runnable {

    private Socket socket;
    private Request request;
    private Response response;
    private Connector connector;

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process() throws IOException, ServletException {
        parseSocket();

    }

    private void parseSocket() throws IOException, ServletException {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        SocketInputStream in = new SocketInputStream(inputStream, Constants.bufferSize);
        request = new Request(in);
        response = new Response(outputStream);

        parseHeaders(in);
    }
    private void parseHeaders(SocketInputStream input)
            throws IOException, ServletException {

        while (true) {

            HttpHeader header = request.getHeader();

            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("httpProcessor.parseHeaders.colon");
                }
            }

            String value = new String(header.value, 0, header.valueEnd);

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
                    throw new ServletException
                            (
                                    ("httpProcessor.parseHeaders.contentLength"));
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

//            else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
//                if (header.valueEquals
//                        (DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
//                    keepAlive = false;
//                    response.setHeader("Connection", "close");
//                }
//                //request.setConnection(header);
//                /*
//                  if ("keep-alive".equalsIgnoreCase(value)) {
//                  keepAlive = true;
//                  }
//                */
//            } else if (header.equals(DefaultHeaders.EXPECT_NAME)) {
//                if (header.valueEquals(DefaultHeaders.EXPECT_100_VALUE))
//                    sendAck = true;
//                else
//                    throw new ServletException("httpProcessor.parseHeaders.unknownExpectation");
//            } else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {
//                //request.setTransferEncoding(header);
//            }

//            request.nextHeader();

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

        // Process the quality values in highest->lowest order (due to
        // negating the Double value when creating the key)
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




}
