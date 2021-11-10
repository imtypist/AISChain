#!/bin/bash

exec 3<aliveip.txt

while read ip<&3;
do
  echo $ip
  sshpass -p Csp@2020 ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@$ip "useradd -G 27 -s /bin/bash -m -p $(perl -e 'print crypt($ARGV[0], "password")' 'intcl2021') intcl"
  sshpass -p u12345, ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@$ip "useradd -G 27 -s /bin/bash -m -p $(perl -e 'print crypt($ARGV[0], "password")' 'intcl2021') intcl"
done
