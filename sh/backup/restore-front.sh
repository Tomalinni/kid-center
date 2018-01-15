#!/bin/sh

#Restores specified archive to nginx html folder

tar -C /usr/share/nginx/html/ -xvf "$1"