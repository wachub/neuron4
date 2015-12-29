#!/bin/sh

prefix=/home/hines/neuron/nrn1078
exec_prefix=/home/hines/neuron/nrn1078/x86_64
NRNBIN=${exec_prefix}/bin
ARCH=x86_64
NEURONHOME=/home/hines/neuron/nrn1078/share/nrn

cd $1

if [ -x ${ARCH}/special ] ; then
	program="./${ARCH}/special"
else
	program="${NRNBIN}/nrniv"
fi

hostname
pwd
shift
shift
echo "time $program $*"
time $program $*

