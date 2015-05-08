# Sapo Broker - Maven Edition
#### Here will be documented variables and goals used to build and execute broker agents and clients, and their dependencies.

To build with maven:

```bash
	mvn clean install
```

To build a debian package:

```bash
	mvn package
```

To run the agent directly from maven:

```bash
	mvn exec:java
```

The package will be at the target directory.



1. **Clean and Build** - in order to build all components please execute **_mvn install_** on the root of the project. If you need to clean all previous builds before a new build execute **_mvn clean_**. You can combine both goals by executing **_mvn clean install_**. You can run these goals on each submodule separately if you only need to update or bundle one of these submodules. All the created jars and necessary files are accessible through the created _target_ folder on each submodule. All inner dependencies follow the global's project version.
1. **Jar with dependencies** - if you include a file named **_with-deps_** on the root of your submodule, a jar with all dependencies is created on the target folder.
1. **Configurable properties (inside the POM or within the command line execution)**
	* **mainClass** - class that will be included on the jar manifest as main Class. In conjunction with jar with dependencies feature you can then execute the resulting jar by calling **_mvn exec:exec_** or **_java -jar [jar-with-dependencies]_**
	* **className** - on some submodules the mainClass can already be defined, sapo-broker-java-client for example, as _<mainClass>pt.com.broker.client.sample.${className}</mainClass>_, being then possible to execute different client types through the command line using the option **_-DclassName=[class name]_**
	* **extraArgs** - extra JVM options specific to each module. They will be concatenated with the default JVM options defined on Sapo's parent POM (-server -Xverify:none -Xms32M -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Dfile.encoding=UTF-8)
	* **extraOptions** - execution options that will be accessible through the _String[] args_ parameter on the main method of the executable class.
1. **Sapo's maven repository (important links)** - All dependencies used originate from our [maven's repository](http://repository.sl.pt/nexus/content/groups/public). Authenticated deployments can be made into [3rd party snapshots](http://repository.sl.pt/nexus/content/repositories/thirdsnapshot/), [3rd party releases](http://repository.sl.pt/nexus/content/repositories/thirdparty/), [Sapo's releases](http://repository.sl.pt/nexus/content/repositories/releases/) and [Sapo's snapshots](http://repository.sl.pt/nexus/content/repositories/snapshots/). 

**The most important central maven repositories are proxied by [Sapo's repository](http://repository.sl.pt/mvn/content/groups/public) so you will only need to use one (Sapo's) repository on your maven settings. You can search and navigate the repository [here](http://repository.sl.pt/mvn).**


## Vagrant

There is a pre-configured [Vagrant](http://www.vagrantup.com/) box with the tools that you need start using sapo broker. 

```bash
	git submodule init
    git submodule update
    cd vagrant
    cp Vagrantfile.orig Vagrantfile
    vagrant plugin install vagrant-share
	vagrant plugin install vagrant-vbguest
	vagrant plugin install vagrant-timezone
    vagrant up
```


## Wireshark 

There is a simple wireshark dissector that helps you debug the broker protocol. The plugin is *work-in-progress* and
the support is very limited. 
