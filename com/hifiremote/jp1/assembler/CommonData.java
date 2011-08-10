package com.hifiremote.jp1.assembler;

public class CommonData
{
  public static final Integer[] to15 = {
    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13, 14, 15 };

  public static final Integer[] to8 = {
    0, 1, 2, 3, 4, 5, 6, 7, 8 };
  
  public static final String[] sigStructs012 = {
    "dev-cmd", "dev-cmd-!dev-!cmd", "dev-!dev-cmd-!cmd", "cmd-dev" };
   
  public static final String[] sigStructs34 = {
    "dev-cmd", "dev-cmd-!dev-!cmd", "dev-!dev-cmd-!cmd", "cmd-!dev", 
    "cmd-dev2", "cmd", "cmd-!cmd", "cmd-!dev-!cmd", "cmd-!dev-dev2", 
    "cmd-!dev-dev2-!cmd", "cmd-dev2-!cmd", "!cmd", "dev", "dev-!cmd", 
    "dev-!dev", "dev-!dev-!cmd", "dev-!dev-cmd", "dev-!dev-dev2", 
    "dev-!dev-dev2-!cmd", "dev-!dev-dev2-cmd", "dev-!dev-dev2-cmd-!cmd", 
    "dev-cmd-!cmd", "dev-cmd-!dev", "dev-cmd-!dev-dev2", 
    "dev-cmd-!dev-dev2-!cmd", "dev-cmd-dev2", "dev-cmd-dev2-!cmd", 
    "dev-dev2", "dev-dev2-!cmd", "dev-dev2-cmd", "dev-dev2-cmd-!cmd", 
    "!dev", "!dev-!cmd", "!dev-cmd", "!dev-cmd-!cmd", "!dev-dev2", 
    "!dev-dev2-!cmd", "!dev-dev2-cmd", "!dev-dev2-cmd-!cmd", "dev2", 
    "dev2-!cmd", "dev2-cmd", "dev2-cmd-!cmd", "devs", "devs-!cmd", 
    "devs-!dev", "devs-!dev-!cmd", "devs-!dev-cmd", "devs-!dev-cmd-!cmd", 
    "devs-!dev-dev2", "devs-!dev-dev2-!cmd", "devs-!dev-dev2-cmd", 
    "devs-!dev-dev2-cmd-!cmd", "devs-cmd", "devs-cmd-!cmd", "devs-cmd-!dev", 
    "devs-cmd-!dev-!cmd", "devs-cmd-!dev-dev2", "devs-cmd-!dev-dev2-!cmd", 
    "devs-cmd-dev2", "devs-cmd-dev2-!cmd", "devs-dev2", "devs-dev2-!cmd", 
    "devs-dev2-cmd", "devs-dev2-cmd-!cmd" }; 


  public static final String[] bitDouble012 = {
    "None", "'0' after every bit", "'1' after every bit", "Double every bit" };

  public static final String[] bitDouble34 = {
    "None", "Bit Double On" };

  public static final String[] repeatType012 = {
    "Minimum", "Forced" };
  
  public static final String[] repeatType34 = {
    "Minimum", "" };
  
  public static final String[] repeatHeld012 = {
    "No", "Yes", "Ch+/-, Vol+/-, FF, Rew", "No data bits in repeat" };
  
  public static final String[] noYes = {
    "No", "Yes" };
  
  public static final String[] leadInStyle = {
    "None", "Same every frame", "1st frame only", "Half-size after 1st" };
  
  public static final String[] leadOutStyle012 = {
    "0 = [-LO]", "1 = [LI], [-LO]", "2 = [OneOn, -LO]", "3 = [LI], [OneOn, -LO]" };

  public static final String[] leadOutStyle34 = {
    "", "[OneOn]", "0 = [-LO]", "2 = [OneOn, -LO]" };
  
  public static final String[] midFrameCode1 = {
    "8D 01 4E", "8D 01 61", "CC FF 7A", "CC 01 C4" };
    
  public static final String[] midFrameCode2 = {
    "F6 01 4E", "F6 01 61", "CD FF 7A", "CD 01 C4" };

  public static final int[] burstOFFoffsets34 = { 52, 57 };
  
  public static final int[] leadinOFFoffsets34 = { 45, 45 };
  
  public static final int[] altLeadoutOffsets34 = { 45, - 40 };

}
