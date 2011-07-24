package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class ProtocolUpgrade.
 */
public class ProtocolUpgrade extends Highlight
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
  
  public ManualProtocol getManualProtocol( Remote remote )
  {
    if ( protocol instanceof ManualProtocol )
    {
      return ( ManualProtocol )protocol;
    }
    
    // This point should never be reached, but if it is then check ProtocolManager for
    // a manual protocol with the pid of this upgrade.
    short[] hex = new short[ 2 ];
    hex[ 0 ] = ( short )( pid / 0x100 );
    hex[ 1 ] = ( short )( pid % 0x100 );

    List< Protocol > protocols = ProtocolManager.getProtocolManager().findByPID( new Hex( hex ) );
    if ( protocols == null )
    {
      return null;
    }
    for ( Protocol p : protocols )
    {
      if ( p.hasCode( remote ) && p instanceof ManualProtocol )
      {
        return ( ManualProtocol )p;
      }
    }
    return null;
  }
  
  public void setManualProtocol( Remote remote )
  {
    Processor proc = remote.getProcessor();
    int fixedDataLength = Protocol.getFixedDataLengthFromCode( proc.getEquivalentName(), code );
    int cmdLength = Protocol.getCmdLengthFromCode( proc.getEquivalentName(), code );
    setManualProtocol( remote, fixedDataLength, cmdLength );
  }
  
  public void setManualProtocol( Remote remote, int fixedDataLength, int cmdLength )
  {
    short[] hex = new short[ 2 ];
    hex[ 0 ] = ( short )( pid / 0x100 );
    hex[ 1 ] = ( short )( pid % 0x100 );
    Hex pidHex = new Hex( hex );

    int cmdType = ManualProtocol.ONE_BYTE;
    if ( cmdLength == 2 )
    {
      cmdType = ManualProtocol.AFTER_CMD;
    }
    if ( cmdLength > 2 )
    {
      cmdType = cmdLength << 4;
    }
    ArrayList< Value > parms = new ArrayList< Value >();
    for ( int i = 0; i < fixedDataLength; i++ )
    {
      parms.add( new Value( 0 ) );
    }
    ManualProtocol mp = new ManualProtocol( ManualProtocol.getDefaultName( pidHex ), pidHex, cmdType, "MSB", 8, parms, new short[ 0 ], 8 );
    mp.setCode( code, remote.getProcessor() );
    ProtocolManager.getProtocolManager().add( mp );
    protocol = mp;
  }

  public Protocol getProtocol()
  {
    return protocol;
  }

  public void setProtocol( Protocol protocol )
  {
    this.protocol = protocol;
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
  
  private Protocol protocol = null;

  /** The used. */
  private boolean used = false;

}
