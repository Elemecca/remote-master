@echo off
call compile
call makejar
call makezip %1
