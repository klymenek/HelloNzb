/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.lame.hellonzb.nntpclient.nioengine;

import java.security.*;
import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class SSLTrustManagerFactory extends TrustManagerFactorySpi
{
	private static final TrustManager DUMMY_TRUST_MANAGER = new X509TrustManager() 
	{
		public X509Certificate[] getAcceptedIssuers() 
		{
			return new X509Certificate[0];
		}
	
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException 
		{
			// Always trust - it is an example.
			// You should do something in the real world.
			//System.err.println("UNKNOWN CLIENT CERTIFICATE: " + chain[0].getSubjectDN());
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException 
		{
			// Always trust - it is an example.
			// You should do something in the real world.
			//System.err.println("UNKNOWN SERVER CERTIFICATE: " + chain[0].getSubjectDN());
		}
	};
	
	public static TrustManager[] getTrustManagers() 
	{
		return new TrustManager[] { DUMMY_TRUST_MANAGER };
	}
	
	@Override
	protected TrustManager[] engineGetTrustManagers()
	{
		return getTrustManagers();
	}

	@Override
	protected void engineInit(KeyStore arg0) throws KeyStoreException
	{
		// do nothing ...
	}

	@Override
	protected void engineInit(ManagerFactoryParameters arg0) throws InvalidAlgorithmParameterException
	{
		// do nothing ...
	}
}
