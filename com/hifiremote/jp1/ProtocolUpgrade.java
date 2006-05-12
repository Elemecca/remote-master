package com.hifiremote.jp1;

public class ProtocolUpgrade
{
  public ProtocolUpgrade( int pid, Hex code, String notes )
  {
    this.pid = pid;
    this.code = code;
    this.notes = notes;
  }
  
  public int getPid(){ return pid; }
  public Hex getCode(){ return code; }
  public String getNotes(){ return notes; }
  public void setNotes( String text ){ notes = text; }

  private int pid;
  private Hex code;
  private String notes;
}
