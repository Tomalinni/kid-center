#!/bin/sh

#Restores content of DB from specified archive

pg_restore -U kidcenteradmin -d kidcenter "$1"