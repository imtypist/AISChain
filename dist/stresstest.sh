#!/bin/bash

java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceAISChain 10000 1500 1 ../src/main/resources/ais.csv
