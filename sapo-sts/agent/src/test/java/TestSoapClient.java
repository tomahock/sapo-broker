import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import pt.com.broker.auth.saposts.SapoSTSService;
import pt.sapo.services.definitions.ESBCredentials;
import pt.sapo.services.definitions.STS;
import pt.sapo.services.definitions.STSSoapSecure;

import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *

 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 28-07-2014.
 */
@Ignore()
public class TestSoapClient {


    @Test()
    public void test() throws MalformedURLException {

        SapoSTSService service = new SapoSTSService();

        STSSoapSecure secure = service.getClient("https://pre-release.services.bk.sapo.pt/STS/");

        ESBCredentials credentials = new ESBCredentials();

        credentials.setESBUsername("luis@luissantos.pt");
        credentials.setESBPassword("OTg#Yv9rfNivr8t$^TsCpgjcLJStY2vYF3G!*UF2699U8");


        String token = secure.getToken(credentials,false);

        System.out.println("Token: "+token);

        Assert.assertNotNull(token);

    }
}
