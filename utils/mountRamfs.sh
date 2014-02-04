#!/bin/bash

sudo mkdir -p /mnt/cassRam
sudo mount -t ramfs -o size=500m ramfs /mnt/cassRam
sudo chown `whoami`:`whoami` /mnt/cassRam

