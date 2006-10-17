package com.hifiremote.jp1;

import java.util.*;

public class ProtocolUpgrade
{
  public ProtocolUpgrade( int pid, Hex code, String notes )
  {
    this.pid = pid;
    this.code = code;
    this.notes = notes;
  }
  
  public ProtocolUpgrade( Properties props )
  {
    pid = Integer.parseInt( props.getProperty( "PID" ));
    code = new Hex( props.getProperty( "Code" ));
    notes = props.getProperty( "Notes" );
  }
  
  public void store( PropertyWriter pw )
  {
    pw.print( "PID", Integer.toString( pid ));
    pw.print( "Code", code.toString());
    if (( notes != null ) && !notes.equals( "" ))
      pw.print( "Notes", notes );
  }
  
  public int getPid(){ return pid; }
  public Hex getCode(){ return code; }
  public String getNotes(){ return notes; }
  public void setNotes( String text ){ notes = text; }
  public boolean isUsed(){ return used; }
  public void setUsed( boolean flag ){ used = flag; }

  private int pid;
  private Hex code;
  private String notes;
  private boolean used = false;
}
