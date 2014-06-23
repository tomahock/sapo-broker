---
layout: broker-documentation
title: Security
---

# Security
* [SSL](#ssl)
* [Authentication](#authentication)
    * [Available Connectors](#connectors)
    * [Creating a custom connector](#custom_connector)
* [Authorization](#authorization) 


## <a name="ssl"></a>Secure Sockets Layer


```html

    <ssl>
        <broker-ssl-port>3390</broker-ssl-port>
        <keystore-location>/path/to/my/mykeystore.jks</keystore-location>
        <keystore-password>password</keystore-password>
        <key-password>mypassword</key-password>
    </ssl>

```

## <a name="authentication"></a>Authentication

### <a name="connectors"></a>Available Authentication Connectors
- SDB (Service Delivery Broker)
    
    
### <a name="custom_connector"></a>Supporting custom Authentication providers

To support your custom Authentication you must implement pt.com.broker.auth.AuthInfoValidator .

Example:

```java

package pt.com.broker.examples;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.AuthInfoValidator;
import pt.com.broker.auth.AuthValidationResult;
import pt.com.broker.auth.ProviderInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CustomAuthenticationProvider implements AuthInfoValidator {

    ProviderInfo providerInfo;

    @Override
    public AuthValidationResult validate(final AuthInfo clientAuthInfo) throws Exception {



        AuthValidationResult result = new AuthValidationResult() {

            @Override
            public boolean areCredentialsValid() {

                String secret = "My super secret password";

                return Arrays.equals(clientAuthInfo.getToken() , secret );

            }

            @Override
            public String getReasonForFailure() {
                return "Invalid Secret";
            }

            @Override
            public List<String> getRoles() {
                List<String> roles = new ArrayList<>(2);

                roles.add("SUPER-USER");

                return roles;
            }
        };


        return result;
    }

    @Override
    public boolean init(ProviderInfo info) {


        providerInfo = info;

        return false;
    }
}
```


Global Config file example:

```html
<global-config>
  
  .... 
  
 <credential-validators>
    <credential-validator provider-name="MyCustomValidator">
      <class>pt.com.broker.examples.CustomAuthenticationProvider</class>
    </credential-validator>
  </credential-validators>
  
</global-config>
```

## <a name="authorization"></a>Authorization

It's possible to define policies to grant access to consumers/producers that meet a specific condition.

- Available conditions
    - Client IP (SubnetPredicate)
    - Client role (RolePredicate)

- Available actions
    - DENY
    - PERMIT
    
- Destination Type
    - TOPIC
    - QUEUE
    - VIRTUAL_QUEUE
    
- Privilege
    - WRITE
    - READ
    
    
### Supported Conditions


```html

   <!-- This condition applies the acl to everyone -->
   <condition condition-type="ALWAYS"/>
   
   <!-- This condition is only applicable to connections coming from the localhost  -->
   <condition condition-type="ADDRESS">
        <address mask="32">127.0.0.1</address>
   </condition>
   
   <!-- This condition is only applicable to authenticated users who was granted a specific role-->
    <condition condition-type="ROLE">
              <role>brk_writer_role</role>
    </condition>
   
   <!-- 
        If you specify more than one condition they will be evaluated with like an disjunction (OR logic gate).
        To change this behavior and force all conditions to be evaluated you must use an AND condition. 
        
        This acl is only applicable to users that have both roles.  
   -->
   <condition condition-type="AND">
         
         <condition condition-type="ROLE">
            <role>brk_writer_role</role>
         </condition>
       
          <condition condition-type="ROLE">
            <role>brk_reader_role</role>
          </condition>
       
   </condition>
   
```

### Examples
    
Deny access to everyone that try to READ or WRITE to "/system/.*"

```html
<global-config> 
    
  <!-- ........ -->
 
  <security-policies>
    <policies>
    
        <!-- ........ -->
        
      <policy policy-name="default">
        <acl>
          <entry action="DENY" destination-type="TOPIC QUEUE VIRTUAL_QUEUE" destination="/system/.*" privilege="READ WRITE">
            <condition condition-type="ALWAYS"/>
          </entry>
        </acl>
      </policy>
  
        <!-- ........ -->
        
    </policies>
  </security-policies>
</global-config>
```


