package connector;

import util.RequestUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;


public abstract class ResponseBase
    implements HttpServletResponse {

    public ResponseBase() {
        headers.put("Server", "Rise/1.0.0");
    }

    protected String protocol = "http/1.1";

    /**
     * Has this response been committed by the application yet?
     */
    protected boolean appCommitted = false;

    protected Map headers = new HashMap();


    /**
     * The buffer through which all of our output bytes are passed.
     */
    protected byte[] buffer = new byte[1024];


    /**
     * The number of data bytes currently in the buffer.
     */
    protected int bufferCount = 0;


    /**
     * Has this response been committed yet?
     */
    protected boolean committed = false;


    /**
     * The Connector through which this Response is returned.
     */
    protected Connector connector = null;


    /**
     * The actual number of bytes written to this Response.
     */
    protected int contentCount = 0;


    /**
     * The content length associated with this Response.
     */
    protected long contentLength = -1;


    /**
     * The content type associated with this Response.
     */
    protected String contentType = null;


    /**
     * The character encoding associated with this Response.
     */
    protected String encoding = null;


    /**
     * Are we currently processing inside a RequestDispatcher.include()?
     */
    protected boolean included = false;


    /**
     * Descriptive information about this Response implementation.
     */
    protected static final String info =
        "org.apache.catalina.connector.ResponseBase/1.0";


    /**
     * The Locale associated with this Response.
     */
    protected Locale locale = Locale.getDefault();


    /**
     * The output stream associated with this Response.
     */
    protected OutputStream output = null;


    /**
     * The Request with which this Response is associated.
     */
    protected Request request = null;

    /**
     * The ServletOutputStream that has been returned by
     * <code>getOutputStream()</code>, if any.
     */
    protected ServletOutputStream stream = null;


    /**
     * Has this response output been suspended?
     */
    protected boolean suspended = false;


    /**
     * The PrintWriter that has been returned by
     * <code>getWriter()</code>, if any.
     */
    protected PrintWriter writer = null;


    /**
     * Error flag. True if the response is an error report.
     */
    protected boolean error = false;

    protected List cookies = new ArrayList();

    protected String message = getStatusMessage(HttpServletResponse.SC_OK);

    protected String characterEncoding = null;

    protected int status = HttpServletResponse.SC_OK;

    protected final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);


    // ------------------------------------------------------------- Properties


    /**
     * Set the application commit flag.
     */
    public void setAppCommitted(boolean appCommitted) {

        this.appCommitted = appCommitted;

    }


    /**
     * Application commit flag accessor.
     */
    public boolean isAppCommitted() {

        return (this.appCommitted || this.committed);

    }


    /**
     * Return the Connector through which this Response will be transmitted.
     */
    public Connector getConnector() {

        return (this.connector);

    }


    /**
     * Set the Connector through which this Response will be transmitted.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {

        this.connector = connector;

    }


    /**
     * Return the number of bytes actually written to the output stream.
     */
    public int getContentCount() {

        return (this.contentCount);

    }


    /**
     * Return the "processing inside an include" flag.
     */
    public boolean getIncluded() {

        return (this.included);

    }


    /**
     * Set the "processing inside an include" flag.
     *
     * @param included <code>true</code> if we are currently inside a
     *  RequestDispatcher.include(), else <code>false</code>
     */
    public void setIncluded(boolean included) {

        this.included = included;

    }


    /**
     * Return descriptive information about this Response implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (ResponseBase.info);

    }


    /**
     * Return the Request with which this Response is associated.
     */
    public Request getRequest() {

        return (this.request);

    }


    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    public void setRequest(Request request) {

        this.request = request;

    }


    /**
     * Return the output stream associated with this Response.
     */
    public OutputStream getStream() {

        return (this.output);

    }


    /**
     * Set the output stream associated with this Response.
     *
     * @param stream The new output stream
     */
    public void setStream(OutputStream stream) {

        this.output = stream;

    }


    /**
     * Set the suspended flag.
     */
    public void setSuspended(boolean suspended) {

        this.suspended = suspended;
        if (stream != null)
            ((ResponseStream) stream).setSuspended(suspended);

    }


    /**
     * Suspended flag accessor.
     */
    public boolean isSuspended() {

        return (this.suspended);

    }


    /**
     * Set the error flag.
     */
    public void setError() {

        this.error = true;

    }


    /**
     * Error flag accessor.
     */
    public boolean isError() {

        return (this.error);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a ServletOutputStream to write the content
     * associated with this Response.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream createOutputStream() throws IOException {

        return (new ResponseStream(this));

    }


    /**
     * Perform whatever actions are required to flush and close the output
     * stream or writer, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishResponse() throws IOException {

        // If no stream has been requested yet, get one so we can
        // flush the necessary headers
        if (this.stream == null) {
            ServletOutputStream sos = getOutputStream();
            sos.flush();
            sos.close();
            return;
        }

        // If our stream is closed, no action is necessary
        if ( ((ResponseStream) stream).closed() )
            return;

        // Flush and close the appropriate output mechanism
        if (writer != null) {
            writer.flush();
            writer.close();
        } else {
            stream.flush();
            stream.close();
        }

        // The underlying output stream (perhaps from a socket)
        // is not our responsibility

    }


    /**
     * Return the content length that was set or calculated for this Response.
     */
    public int getContentLength() {

        return (int) this.contentLength;

    }


    /**
     * Return the content type that was set or calculated for this response,
     * or <code>null</code> if no content type was set.
     */
    public String getContentType() {

        return (this.contentType);

    }


    /**
     * Return a PrintWriter that can be used to render error messages,
     * regardless of whether a stream or writer has already been acquired.
     */
    public PrintWriter getReporter() {

        if (isError()) {

            try {
                if (this.stream == null)
                    this.stream = createOutputStream();
            } catch (IOException e) {
                return null;
            }
            return (new PrintWriter(this.stream));

        } else {

            if (this.stream != null) {
                return null;
            } else {
                try {
                    return (new PrintWriter(getOutputStream()));
                } catch (IOException e) {
                    return null;
                }
            }

        }

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        // buffer is NOT reset when recycling
        bufferCount = 0;
        committed = false;
        appCommitted = false;
        suspended = false;
        // connector is NOT reset when recycling
        contentCount = 0;
        contentLength = -1;
        contentType = null;
        encoding = null;
        included = false;
        locale = Locale.getDefault();
        output = null;
        request = null;
        stream = null;
        writer = null;
        error = false;

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Write the specified byte to our output stream, flushing if necessary.
     *
     * @param b The byte to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(int b) throws IOException {

        if (suspended)
            throw new IOException
                (("responseBase.write.suspended"));

        if (bufferCount >= buffer.length)
            flushBuffer();
        buffer[bufferCount++] = (byte) b;
        contentCount++;

    }


    /**
     * Write <code>b.length</code> bytes from the specified byte array
     * to our output stream.  Flush the output stream as necessary.
     *
     * @param b The byte array to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(byte b[]) throws IOException {

        if (suspended)
            throw new IOException("responseBase.write.suspended");

        write(b, 0, b.length);

    }


    /**
     * Write <code>len</code> bytes from the specified byte array, starting
     * at the specified offset, to our output stream.  Flush the output
     * stream as necessary.
     *
     * @param b The byte array containing the bytes to be written
     * @param off Zero-relative starting offset of the bytes to be written
     * @param len The number of bytes to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(byte b[], int off, int len) throws IOException {

        if (suspended)
            throw new IOException
                (("responseBase.write.suspended"));

        // If the whole thing fits in the buffer, just put it there
        if (len == 0)
            return;
        if (len <= (buffer.length - bufferCount)) {
            System.arraycopy(b, off, buffer, bufferCount, len);
            bufferCount += len;
            contentCount += len;
            return;
        }

        // Flush the buffer and start writing full-buffer-size chunks
        flushBuffer();
        int iterations = len / buffer.length;
        int leftoverStart = iterations * buffer.length;
        int leftoverLen = len - leftoverStart;
        for (int i = 0; i < iterations; i++)
            write(b, off + (i * buffer.length), buffer.length);

        // Write the remainder (guaranteed to fit in the buffer)
        if (leftoverLen > 0)
            write(b, off + leftoverStart, leftoverLen);

    }


    // ------------------------------------------------ ServletResponse Methods


    /**
     * Flush the buffer and commit this response.
     *
     * @exception IOException if an input/output error occurs
     */
    public void flushBuffer() throws IOException {

        committed = true;
        if (bufferCount > 0) {
            try {
                output.write(buffer, 0, bufferCount);
            } catch(IOException ioe) {
                // An IOException on a write is almost always due to
                // the remote client aborting the request.  Wrap this
                // so that it can be handled better by the error dispatcher.
                throw ioe;
            } finally {
                bufferCount = 0;
            }
        }

    }


    /**
     * Return the actual buffer size used for this Response.
     */
    public int getBufferSize() {

        return (buffer.length);

    }


    /**
     * Return the character encoding used for this Response.
     */
    public String getCharacterEncoding() {

        if (encoding == null)
            return ("ISO-8859-1");
        else
            return (encoding);

    }


    /**
     * Return the servlet output stream associated with this Response.
     *
     * @exception IllegalStateException if <code>getWriter</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream getOutputStream() throws IOException {

        if (writer != null)
            throw new IllegalStateException
                (("responseBase.getOutputStream.ise"));

        if (stream == null)
            stream = createOutputStream();
        ((ResponseStream) stream).setCommit(true);
        return (stream);

    }


    /**
     * Return the Locale assigned to this response.
     */
    public Locale getLocale() {

        return (locale);

    }


    /**
     * Return the writer associated with this Response.
     *
     * @exception IllegalStateException if <code>getOutputStream</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    public PrintWriter getWriter() throws IOException {

        if (writer != null)
            return (writer);

        if (stream != null)
            throw new IllegalStateException
                (("responseBase.getWriter.ise"));

        ResponseStream newStream = (ResponseStream) createOutputStream();
        newStream.setCommit(false);
        OutputStreamWriter osr =
            new OutputStreamWriter(newStream, getCharacterEncoding());
        writer = new ResponseWriter(osr, newStream);
        stream = newStream;
        return (writer);

    }


    /**
     * Has the output of this response already been committed?
     */
    public boolean isCommitted() {

        return (committed);

    }


    /**
     * Clear any content written to the buffer.
     *
     * @exception IllegalStateException if this response has already
     *  been committed
     */
    public void reset() {

        if (committed)
            throw new IllegalStateException
                (("responseBase.reset.ise"));

        if (included)
            return;     // Ignore any call from an included servlet

        if (stream != null)
            ((ResponseStream) stream).reset();
        bufferCount = 0;
        contentLength = -1;
        contentType = null;

    }


    /**
     * Reset the data buffer but not any status or header information.
     *
     * @exception IllegalStateException if the response has already
     *  been committed
     */
    public void resetBuffer() {

        if (committed)
            throw new IllegalStateException("responseBase.resetBuffer.ise");

        bufferCount = 0;

    }


    /**
     * Set the buffer size to be used for this Response.
     *
     * @param size The new buffer size
     *
     * @exception IllegalStateException if this method is called after
     *  output has been committed for this response
     */
    public void setBufferSize(int size) {

        if (committed || (bufferCount > 0))
            throw new IllegalStateException
                (("responseBase.setBufferSize.ise"));

        if (buffer.length >= size)
            return;
        buffer = new byte[size];

    }


    /**
     * Set the content length (in bytes) for this Response.
     *
     * @param length The new content length
     */
    public void setContentLength(int length) {

        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        this.contentLength = length;

    }


    /**
     * Set the content type for this Response.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {

        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        this.contentType = type;
        if (type.indexOf(';') >= 0) {
            encoding = RequestUtil.parseCharacterEncoding(type);
            if (encoding == null)
                encoding = "ISO-8859-1";
        } else {
            if (encoding != null)
                this.contentType = type + ";charset=" + encoding;
        }

    }


    /**
     * Set the Locale that is appropriate for this response, including
     * setting the appropriate character encoding.
     *
     * @param locale The new locale
     */
    public void setLocale(Locale locale) {

        if (isCommitted())
            return;

        if (included)
            return;

        String language = locale.getLanguage();
        if ((language != null) && (language.length() > 0)) {
            String country = locale.getCountry();
            StringBuffer value = new StringBuffer(language);
            if ((country != null) && (country.length() > 0)) {
                value.append('-');
                value.append(country);
            }
            setHeader("Content-Language", value.toString());
        }

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

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
