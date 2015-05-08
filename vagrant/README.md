This is a ready to use vagrant box with all dependencies and tools needed to build and use all supported broker clients.

Install vagrant on your system;

Copy the vagrant file:

```bash
	cp Vagrantfile.orig Vagrantfile
```

Init git submodules:

```bash
    git submodule init
    git submodule update
```

Install the vagrant plugins:
	- vagrant-share (vagrant plugin install vagrant-share)
	- vagrant-vbguest (vagrant plugin install vagrant-vbguest)
	- vagrant-timezone (vagrant plugin install vagrant-timezone)

```bash
	vagrant plugin install vagrant-share
	vagrant plugin install vagrant-vbguest
	vagrant plugin install vagrant-timezone
```
