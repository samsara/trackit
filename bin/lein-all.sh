#!/bin/bash

export CUR=$(pwd)
export BASE=$(dirname $0)/..

function leinit(){
    prj=$1
    cd $BASE/$1
    shift
    echo "(*) --------------------------------------------------------------"
    echo "(*) running $prj : 'lein $*'"
    echo "(*) --------------------------------------------------------------"
    lein $*
    cd $CUR
}


leinit trackit-core $*
leinit trackit-ganglia $*
leinit trackit-graphite $*
leinit trackit-influxdb $*
leinit trackit-riemann $*
leinit trackit-statsd $*
leinit trackit-all $*
