#!/bin/bash

# Clumsy way to support builds for i386 and amd64:
#  if building pkg that is marked has amd64 on a i386 system..
#  then it should be a i386 pkg.
function fix_arch() {
    file=$1

    x86=`grep amd64 $file`
    arch=`uname -m`
    if [ "${x86}" -a $arch = 'i686' ]; then
        sed -i 's/amd64/i386/g' $file
    fi

}

function make_deb() {
    BDIR=/tmp/debroot-$$
    mkdir -p $BDIR/DEBIAN
    mkdir -p $BDIR/usr/share/doc/$1


    cp debian/$1.control $BDIR/DEBIAN/control
    cp debian/changelog $BDIR/DEBIAN/changelog
    cp -p debian/$1.postinst  $BDIR/DEBIAN/postinst  > /dev/null 2>&1
    cp -p debian/$1.conffiles $BDIR/DEBIAN/conffiles > /dev/null 2>&1
    cp -p debian/$1.dirs      $BDIR/DEBIAN/dirs      > /dev/null 2>&1

    # directories must have 755 permissions
    find $BDIR -type d -exec chmod 0755 {} \;

    chmod 755 $BDIR/DEBIAN/postinst > /dev/null 2>&1

    cp debian/copyright $BDIR/usr/share/doc/$1

    mkdir -p $BDIR/servers/adwords
    cp -a $2/* $BDIR/

    fix_arch $BDIR/DEBIAN/control
    fakeroot dpkg-deb --build $BDIR .

    rm -rf $BDIR/
}


function make_libsapo-broker2()
{
    DIR="/tmp/backend-$$"
    make clean
    make all
    PREFIX="$DIR" make install

    make_deb 'libsapo-broker2' $DIR
    rm -rf $DIR
}

echo 'make shure the following *debian* packages are installed: '
echo '  make    fakeroot    gcc     g++'
echo '  libc-dev   libprotobuf-dev  libprotobuf3'
echo
echo

make_libsapo-broker
