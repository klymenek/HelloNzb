
package at.lame.hellonzb.statistics;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for downloadRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="downloadRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" form="unqualified"/>
 *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string" form="unqualified"/>
 *         &lt;element name="session" type="{http://www.w3.org/2001/XMLSchema}string" form="unqualified"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}date" form="unqualified"/>
 *         &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}time" form="unqualified"/>
 *         &lt;element name="bytes" type="{http://www.w3.org/2001/XMLSchema}integer" form="unqualified"/>
 *         &lt;element name="connections" type="{http://www.w3.org/2001/XMLSchema}integer" form="unqualified"/>
 *         &lt;element name="ssl" type="{http://www.w3.org/2001/XMLSchema}boolean" form="unqualified"/>
 *         &lt;element name="speedlimit" type="{http://www.w3.org/2001/XMLSchema}boolean" form="unqualified"/>
 *         &lt;element name="par2" type="{http://www.w3.org/2001/XMLSchema}boolean" form="unqualified"/>
 *         &lt;element name="unrar" type="{http://www.w3.org/2001/XMLSchema}boolean" form="unqualified"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "downloadRequestType", propOrder = {

})
public class DownloadRequestType implements ServiceRequestType {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String uuid;
    @XmlElement(required = true)
    protected String session;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar date;
    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar time;
    @XmlElement(required = true)
    protected BigInteger bytes;
    @XmlElement(required = true)
    protected BigInteger connections;
    protected boolean ssl;
    protected boolean speedlimit;
    protected boolean par2;
    protected boolean unrar;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the session property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSession() {
        return session;
    }

    /**
     * Sets the value of the session property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSession(String value) {
        this.session = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the bytes property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBytes() {
        return bytes;
    }

    /**
     * Sets the value of the bytes property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBytes(BigInteger value) {
        this.bytes = value;
    }

    /**
     * Gets the value of the connections property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getConnections() {
        return connections;
    }

    /**
     * Sets the value of the connections property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setConnections(BigInteger value) {
        this.connections = value;
    }

    /**
     * Gets the value of the ssl property.
     * 
     */
    public boolean isSsl() {
        return ssl;
    }

    /**
     * Sets the value of the ssl property.
     * 
     */
    public void setSsl(boolean value) {
        this.ssl = value;
    }

    /**
     * Gets the value of the speedlimit property.
     * 
     */
    public boolean isSpeedlimit() {
        return speedlimit;
    }

    /**
     * Sets the value of the speedlimit property.
     * 
     */
    public void setSpeedlimit(boolean value) {
        this.speedlimit = value;
    }

    /**
     * Gets the value of the par2 property.
     * 
     */
    public boolean isPar2() {
        return par2;
    }

    /**
     * Sets the value of the par2 property.
     * 
     */
    public void setPar2(boolean value) {
        this.par2 = value;
    }

    /**
     * Gets the value of the unrar property.
     * 
     */
    public boolean isUnrar() {
        return unrar;
    }

    /**
     * Sets the value of the unrar property.
     * 
     */
    public void setUnrar(boolean value) {
        this.unrar = value;
    }

}
