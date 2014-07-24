package pt.com.broker.auth.saposts;

import org.apache.commons.lang3.StringUtils;
import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.ProviderInfo;
import pt.sapo.services.definitions.STS;
import pt.sapo.services.definitions.STSSoapSecure;

import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * SapoSTSProvider implements a credentials provider for Sapo STS.
 * 
 */
public class SapoSTSProvider implements CredentialsProvider
{
    public static final String DEFAULT_BASE_URL = "https://services.bk.sapo.pt/STS/";

	private final String providerName = "SapoSTS";

	private final String username;
	private final String password;
    private final String stsLocation;

    private final SAPOStsToken stsToken;

	public SapoSTSProvider(String username, String password)
	{
		this(username, password,DEFAULT_BASE_URL);
	}

	public SapoSTSProvider(String username, String password, String stsLocation)
	{
		if (StringUtils.isBlank(stsLocation))
		{
			throw new IllegalArgumentException("STS Location URL must not be blank");
		}

		this.username = username;
		this.password = password;
        this.stsLocation = stsLocation;
        stsToken = new SAPOStsToken(getClient(stsLocation));

	}

	@Override
	public AuthInfo getCredentials() throws Exception
	{
		String strToken = stsToken.getToken(username, password);

		byte[] token = strToken.getBytes(Charset.forName("UTF-8"));


		AuthInfo aui = new AuthInfo(username, null, token, providerName);
		return aui;
	}

	@Override
	public boolean init(ProviderInfo info)
	{
		return true;
	}

	@Override
	public String getAuthenticationType()
	{
		return providerName;
	}


	@Override
	public String toString()
	{
		return "SapoSTSProvider [providerName=" + providerName + ", stsLocation=" + stsLocation + "]";
	}

    protected STSSoapSecure getClient(String base_url){

        URL url = null;

        try {

            url = STSSoapSecure.class.getClassLoader().getResource("STS.wsdl");

            STS sts = new STS(url);



            STSSoapSecure secure = sts.getSTSSoapSecure();

            BindingProvider bp = (BindingProvider) secure;

            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, base_url);


            return secure;


        } catch (Throwable e) {
            throw new RuntimeException(e);
        }


    }
}