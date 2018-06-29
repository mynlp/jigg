#!/bin/bash

# upgrade c++
# add repository
sudo add-apt-repository ppa:ubuntu-toolchain-r/test -y   
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 1E9377A2BA9EF27F

sudo apt update -y && sudo apt install g++-4.9 gcc-4.9 -y

sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.8 10
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.9 20
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/g++-4.8 10
sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.9 20

sudo rm /usr/bin/cpp

sudo update-alternatives --install /usr/bin/cpp cpp /usr/bin/cpp-4.8 10
sudo update-alternatives --install /usr/bin/cpp cpp /usr/bin/cpp-4.9 20
sudo update-alternatives --install /usr/bin/cc cc /usr/bin/gcc 30
sudo update-alternatives --set cc /usr/bin/gcc
sudo update-alternatives --install /usr/bin/c++ c++ /usr/bin/g++ 30
sudo update-alternatives --set c++ /usr/bin/g++
