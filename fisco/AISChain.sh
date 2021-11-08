#!/bin/bash

# Please firstly prepare "build_chain.sh" and "fisco-bcos" binary before running this script. 
# https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/installation.html

num_of_nodes=$1

bash nodes/127.0.0.1/stop_all.sh

rm -rf nodes/

bash build_chain.sh -l 127.0.0.1:$num_of_nodes -p 30300,20200,8545

bash nodes/127.0.0.1/start_all.sh

cp -r nodes/127.0.0.1/sdk/* ~/java-sdk-demo/dist/conf/

