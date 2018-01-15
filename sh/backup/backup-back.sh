#!/bin/sh

#Backs up content of backend app part to archive with current timestamp value

tar -C /var/kidcenter/ -cvzf /opt/kidcenter/backup/back-`date +%F_%H-%M-%S`.tar.gz bin lib