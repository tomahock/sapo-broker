# SSL Advanced Usage


## Connecting using a custom CA certificate
```java

        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
       
        InputStream caInput = new BufferedInputStream(new FileInputStream("sapo.crt"));
        Certificate ca;
        try {
         ca = cf.generateCertificate(caInput);
         System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
         caInput.close();
        }
        
        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);
        
        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);
        
        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        
        
         SslBrokerClient bk = new SslBrokerClient();
               
         bk.addServer("broker.wallet.pt",3390); // 3390 broker SSL port
               
         // by default it uses the jvm certificate authorities but you can change it
         bk.setContext(context);
         
        // ... connecting ... 
               
```
> Based on [http://developer.android.com/training/articles/security-ssl.html](http://developer.android.com/training/articles/security-ssl.html).

