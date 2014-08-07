---
layout: broker-documentation
title: SDB Authentication
site_root:  /
tags:
---

As mentioned before, is possible to protect the broker using a authentication provider.
One of the available providers validates ESBTokens provided by the [Service Delivery Broker (SDB)](http://sdb.sapo.pt/).


### Client authentication
<div>
    <img  alt="SDB Authentication" src="{{ site.url }}/broker/SDBAuthentication.png" style="display: block;" class="push-center" />
</div>





### Agent configuration

```xml
<global-config>


      <!-- .... more configs .... -->

      <credential-validators>
            <credential-validator provider-name="SapoSTS">
              <class>pt.com.broker.auth.saposts.SapoSTSAuthInfoValidator</class>
              <provider-params>
                  <sts>
                    <!-- .... SDB STS endpoint .... -->
                    <sts-location>https://pre-release.services.bk.sapo.pt/STS/</sts-location>
                    
                    <!-- .... SDB ESBTOken with permissions to validate other users credentials  .... -->
                    <sts-token>29b54e8fcc92fd3ceacaf8d807df335d49c7474343325ca83beda4e268b48883</sts-token>
                  </sts>
                </provider-params>
            </credential-validator>
      </credential-validators>
</global-config>

```

