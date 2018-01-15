#!/bin/sh

#Backs up content of DB to archive with current timestamp value

pg_dump -U kidcenteradmin -Fc kidcenter > /opt/kidcenter/backup/db-`date +%F_%H-%M-%S`.dmp