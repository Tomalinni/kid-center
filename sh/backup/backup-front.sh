#!/bin/sh

#Backs up content of nginx html folder to archive with current timestamp value

tar -C /usr/share/nginx/html/ -cvzf /opt/kidcenter/backup/front-`date +%F_%H-%M-%S`.tar.gz .