#!/bin/bash

set +x

tar xvf iv-18.tar.gz
tar xvf nrn-7.3.tar.gz
mkdir neuroConstruct
cd neuroConstruct
tar xvf ../neuroConstruct.tar.gz
cd ..
mv iv-18 iv
mv nrn-7.3 nrn

export http_proxy=http://cs11b029:123@192.168.36.22:3128/
export https_proxy=http://cs11b029:123@192.168.36.22:3128/
export ftp_proxy=http://cs11b029:123@192.168.36.22:3128/

sudo -E apt-get install python-dev ncurses-dev xorg-dev openjdk-7-jdk

cd iv
echo "Setting up IV"
sleep 2
./configure --prefix=$HOME/neuron/iv 
make -j2
make install

cd ..
cd nrn
echo "Setting up NRN"
sleep 2
./configure --prefix=$HOME/neuron/nrn --with-iv=$HOME/neuron/iv --with-nrnpython=/usr/bin/python
make -j2
make install

cd ~
echo "Setting up variables"
sleep 2
echo "export PATH=$PATH:$HOME/neuron/nrn/x86_64/bin" >> ~/.bashrc
echo "export PYTHONPATH=$PYTHONPATH:$HOME/neuron/nrn/lib/python" >> ~/.bashrc
source ~/.bashrc
