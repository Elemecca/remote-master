package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class ProtocolUpgrade.
 */
public class ProtocolUpgrade
{

  /**
   * Instantiates a new protocol upgrade.
   * 
   * @param pid
   *          the pid
   * @param code
   *          the code
   * @param notes
   *          the notes
   */
  public ProtocolUpgrade( int pid, Hex code, String notes )
  {
    this.pid = pid;
    this.code = code;
    this.notes = notes;
  }

  /**
   * Instantiates a new protocol upgrade.
   * 
   * @param props
   *          the props
   */
  public ProtocolUpgrade( Properties props )
  {
    pid = Integer.parseInt( props.getProperty( "PID" ) );
    code = new Hex( props.getProperty( "Code" ) );
    notes = props.getProperty( "Notes" );
  }

  /**
   * Store.
   * 
   * @param pw
   *          the pw
   */
  public void store( PropertyWriter pw )
  {
    pw.print( "PID", Integer.toString( pid ) );
    pw.print( "Code", code.toString() );
    if ( ( notes != null ) && !notes.equals( "" ) )
      pw.print( "Notes", notes );
  }

  /**
   * Gets the pid.
   * 
   * @return the pid
   */
  public int getPid()
  {
    return pid;
  }

  public void setCode( Hex hex )
  {
    this.code = hex;
  }

  /**
   * Gets the code.
   * 
   * @return the code
   */
  public Hex getCode()
  {
    return code;
  }

  /**
   * Gets the notes.
   * 
   * @return the notes
   */
  public String getNotes()
  {
    return notes;
  }

  /**
   * Sets the notes.
   * 
   * @param text
   *          the new notes
   */
  public void setNotes( String text )
  {
    notes = text;
  }

  /**
   * Checks if is used.
   * 
   * @return true, if is used
   */
  public boolean isUsed()
  {
    return used;
  }

  /**
   * Sets the used.
   * 
   * @param flag
   *          the new used
   */
  public void setUsed( boolean flag )
  {
    used = flag;
  }

  /** The pid. */
  private int pid;

  /** The code. */
  private Hex code;

  /** The notes. */
  private String notes;

  /** The used. */
  private boolean used = false;
}
