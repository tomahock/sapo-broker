---
layout: broker-documentation
title: PHP Extension
---

The following code only works with php sapobroker extension 0.4 using PHP 5.

## Compiling

- Debian dependencies
  * php5-dev
  * libsapo-broker2-dev (install it from sapo debian repository)
  * build-essential

- Compiling
  * phpize
  * ./configure
  * make
  * make install


## Creating a Debian Package

- Debian dependencies
  * php5-dev
  * libsapo-broker2-dev (install it from sapo debian repository)
  * build-essential
  * dh-make-php
  * debhelper
  
- Building package
  * ./build




## Connecting

```php
<?php

// instantiate the broker handle

$broker = broker_init("127.0.0.1", SB_PORT, SB_TCP, SB_PROTOBUF);

// adding another server 

broker_add_server($broker, "127.0.0.1", SB_PORT, SB_TCP, SB_PROTOBUF);


/* ... do some work */

/* allways close your broker connection */
broker_destroy($broker);


```


## Sending Message to a Topic

```php
<?php
// ... connect to broker ...

$msg = "Hello World!";
   
broker_publish($broker, "/test/foo", $msg);



```

## Sending Message to a Queue

```php
<?php

// ... connect to broker ...

$msg = "Hello World!";
   
broker_enqueue($broker, "/test/foo", $msg);

```




## Subscribing a Queue


```php
<?php
// ... connect to broker ...

$ret = broker_subscribe_queue($broker, "/test/foo", 0);

$timeout = 1000;

while (true) {

    /* 
        This call will block until a timeout is reached,
        an error occurred or a message is received.
    */
    $msg = broker_receive($broker, $timeout); 
    
    if($msg !== false){
         
        $msg_data = broker_msg_decode($msg);
        echo "Got message: " . print_r($msg_data, true) . "\n";
        
        /* ... do some work ... */
        
        broker_msg_ack($msg); // don't forget to acknowledge the message reception
         
    }
    
    if( /*... exit condition ...*/ ){
        break;
    }

}

```


## Subscribing a topic

```php
<?php
// ... connect to broker ...

$ret = broker_subscribe_topic($broker, "/test/foo");

$timeout = 1000;

while (true) {

     
    /* 
      This call will block until a timeout is reached,
      an error occurred or a message is received.
    */
    $msg = broker_receive($broker, $timeout); 
    
    if($msg !== false){
    
      $msg_data = broker_msg_decode($msg);
      echo "Got message: " . print_r($msg_data, true) . "\n";
      
       
    }
    
    
    if( /*... exit condition ...*/){
      break;
    }


}

```


## Subscribing a Queue with Auto Acknowledge


```php
<?php

// ... connect to broker ...

$ret = broker_subscribe_queue($broker, "/test/foo", 0);

$timeout = 1000;

while (true) {

    /* 
        This call will block until a timeout is reached,
        an error occurred or a message is received.
    */
    $msg = broker_receive($broker, $timeout, true); 
    
    if($msg !== false){
         
        echo "Got message: " . print_r($msg, true) . "\n";
         
    }
    
    
    if( /*... exit condition ...*/){
        break;
    }

}

```


