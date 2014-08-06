# Sapo Broker - PHP Extension


The following code only works with php sapobroker extension 0.4.

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

