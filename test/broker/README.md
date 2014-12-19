# Test batery for the broker agent and client.

## Installation

Self signed certificate and keystore:

```
keytool -genkey -keyalg RSA -alias brokercert -keystore brokerkeystore.jks -storepass 123qwe123 -validity 360 -keysize 2048
```

In order to run all tests, there are a few dependencies required. For the SSL tests, a keystore must be provived on the configuration file. To setup a java keystore follow this steps:

```
keytool -keystore brokerkeystore -genkey -alias broker
```
Enter the required information.

```
keytool -keystore brokerkeystore -certreq -alias broker -keyalg rsa -file broker.csr 
```

This command generates a new certificate. We import the certificate to the keystore with the
command:

```
keytool -import -keystore brokerkeystore -file broker.cer -alias broker
```