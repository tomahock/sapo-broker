---
layout: broker-documentation
title: Agent plugins
site_root:  /
tags:
---

The plugin architecture uses the [Java Service Loader](http://docs.oracle.com/javase/tutorial/ext/basics/spi.html) to load
custom plugins when the agent starts.

You must implement the **pt.com.broker.core.AgentPlugin** interface and create a file named pt.com.broker.core.AgentPlugin
inside the **META-INF/services** directory containing you implementation full class name.
 
## CustomPlugin.java
```java

package pt.com.broker.samples;

import pt.com.broker.core.AgentPlugin;
import pt.com.gcs.messaging.Gcs;

public class  CustomPlugin implements AgentPlugin {

    @Override
    public void start(Gcs gcs) {
    
        // do something
        
    }
    
}

```

### META-INF/services/pt.com.broker.core.AgentPlugin

```
pt.com.broker.samples.CustomPlugin

```

Finally you must put the resulting jar inside the agent class-path.
**You must run your code inside a thread otherwise the broker agent will block the execution and willnot load the 
 remaining plugins.** 
 
Check our [GitHub](https://github.com/sapo/sapo-broker/tree/master/agent-plugins) repository to see the available plugins.

