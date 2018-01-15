#!/bin/sh

#Restores content of DB from specified archive

dropdb kidcenter_tmp
createdb -O vagrant kidcenter_tmp
pg_restore -U vagrant -d kidcenter_tmp somedump.dmp