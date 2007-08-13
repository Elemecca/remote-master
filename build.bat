@echo off
call compile -g
call makejar
call makezip %1
