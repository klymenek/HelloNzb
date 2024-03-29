package at.lame.hellonzb.statistics;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.5.2
 * 2012-03-06T02:52:36.386+01:00
 * Generated source version: 2.5.2
 * 
 */
@WebServiceClient(name = "HelloNzb", 
                  wsdlLocation = "http://hellonzb.sourceforge.net/stats/server.php?wsdl",
                  targetNamespace = "urn:hellonzb") 
public class HelloNzbService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("urn:hellonzb", "HelloNzb");
    public final static QName HelloNzbPort = new QName("urn:hellonzb", "HelloNzbPort");
    static {
        URL url = null;
        try {
            url = new URL("http://hellonzb.sourceforge.net/stats/server.php?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public HelloNzbService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public HelloNzbService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public HelloNzbService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns HelloNzbPortType
     */
    @WebEndpoint(name = "HelloNzbPort")
    public HelloNzbPortType getHelloNzbPort() {
        return super.getPort(HelloNzbPort, HelloNzbPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns HelloNzbPortType
     */
    @WebEndpoint(name = "HelloNzbPort")
    public HelloNzbPortType getHelloNzbPort(WebServiceFeature... features) {
        return super.getPort(HelloNzbPort, HelloNzbPortType.class, features);
    }

}
