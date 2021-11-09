#!/bin/bash

type=$1

rm log/*.log

if [[ $type -eq 1 ]]
then
  txns=$2
  qps=$3
  java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceAISChain $txns $qps 1 ../src/main/resources/ais.csv
elif [[ $type -eq 2 ]]
then
  qps=$2
  java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceAISChainContinuous $qps 1 ../src/main/resources/ais.csv
else
  echo "check your parameters"
fi
