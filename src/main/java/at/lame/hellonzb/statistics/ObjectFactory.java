
package at.lame.hellonzb.statistics;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the at.lame.hellonzb.statistics package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Startup_QNAME = new QName("urn:hellonzb", "startup");
    private final static QName _StartupResponse_QNAME = new QName("urn:hellonzb", "startupResponse");
    private final static QName _Download_QNAME = new QName("urn:hellonzb", "download");
    private final static QName _DownloadResponse_QNAME = new QName("urn:hellonzb", "downloadResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: at.lame.hellonzb.statistics
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DownloadRequestType }
     * 
     */
    public DownloadRequestType createDownloadRequestType() {
        return new DownloadRequestType();
    }

    /**
     * Create an instance of {@link StartupResponseType }
     * 
     */
    public StartupResponseType createStartupResponseType() {
        return new StartupResponseType();
    }

    /**
     * Create an instance of {@link DownloadResponseType }
     * 
     */
    public DownloadResponseType createDownloadResponseType() {
        return new DownloadResponseType();
    }

    /**
     * Create an instance of {@link StartupRequestType }
     * 
     */
    public StartupRequestType createStartupRequestType() {
        return new StartupRequestType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartupRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:hellonzb", name = "startup")
    public JAXBElement<StartupRequestType> createStartup(StartupRequestType value) {
        return new JAXBElement<StartupRequestType>(_Startup_QNAME, StartupRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartupResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:hellonzb", name = "startupResponse")
    public JAXBElement<StartupResponseType> createStartupResponse(StartupResponseType value) {
        return new JAXBElement<StartupResponseType>(_StartupResponse_QNAME, StartupResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DownloadRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:hellonzb", name = "download")
    public JAXBElement<DownloadRequestType> createDownload(DownloadRequestType value) {
        return new JAXBElement<DownloadRequestType>(_Download_QNAME, DownloadRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DownloadResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:hellonzb", name = "downloadResponse")
    public JAXBElement<DownloadResponseType> createDownloadResponse(DownloadResponseType value) {
        return new JAXBElement<DownloadResponseType>(_DownloadResponse_QNAME, DownloadResponseType.class, null, value);
    }

}
