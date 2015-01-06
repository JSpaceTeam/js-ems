#!/bin/base

CURRDIR=`pwd`
BASEDIR=$CURRDIR/..
DISTDIR=$BASEDIR/target/dist

package(){
    rm -rf $DISTDIR/*
    mkdir -p $DISTDIR

    #1 package all sub modules
    cd $BASEDIR
    sbt xitrum-package

    #2 collect all modules to root project.
    if [ -d $BASEDIR/target/xitrum ]; then
        cp -rf $BASEDIR/target/xitrum/* $DISTDIR
    fi

    dirs=$(ls -l $BASEDIR |awk '/^d/ {print $NF}')
    for dir in $dirs
        do
           if [ -d $BASEDIR/$dir/target/xitrum ]; then
              cp -rf $BASEDIR/$dir/target/xitrum/* $DISTDIR
           fi
        done
}

package