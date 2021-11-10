#!/bin/bash

action=$1
username="intcl"
passwd="intcl2021"


exec 3<downip.txt

while read ip<&3;
do
  sshpass -p $passwd ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $username@$ip "bash ~/fisco/"$ip"/"$action"_all.sh"
done
