
package at.lame.hellonzb.exceptions;


/**
 * This class extends a standard Java Exception class.
 * It is used to handle errors raised by the nntp client.
 * 
 * @author Matthias F. Brandstetter
 */
public class NntpClientException extends Exception
{
	/** Exception type: unknown host name */
	public static final int UNKNOWN_HOST = 1;
	
	/** Exception type: could not open socket */
	public static final int SOCKET_ERROR = 2;
	
	/** Exception type: authentication failed */
	public static final int AUTH_FAILED = 3;
	
	/** Serial Version UID */
	private static final long serialVersionUID = 1;
	
	/** The nntp exception type of this exception object */
	private int exType;

	
	/**
	 * Default constructor.
	 */
	public NntpClientException(int t)
	{
		super();
		exType = t;
	}

	/**
	 * Class constructor that receives an error text.
	 * 
	 * @param s The error text of this exception object
	 */
	public NntpClientException(String s, int t)
	{
		super(s);
		exType = t;
	}
	
	/**
	 * Return the type of this exception object.
	 * 
	 * @return The exception type
	 */
	public int getExType()
	{
		return exType;
	}
}
