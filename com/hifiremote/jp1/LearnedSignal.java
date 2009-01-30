package com.hifiremote.jp1;

import com.hifiremote.decodeir.*;
import java.io.*;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignal.
 */
public class LearnedSignal
{
  
  /**
   * Instantiates a new learned signal.
   * 
   * @param keyCode the key code
   * @param deviceButtonIndex the device button index
   * @param data the data
   * @param notes the notes
   */
  public LearnedSignal( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.deviceButtonIndex = deviceButtonIndex;
    this.data = data;
    this.notes = notes;
  }

  /**
   * Instantiates a new learned signal.
   * 
   * @param properties the properties
   */
  public LearnedSignal( Properties properties )
  {
    keyCode = Integer.parseInt( properties.getProperty( "KeyCode" ));
    deviceButtonIndex = Integer.parseInt( properties.getProperty( "DeviceButtonIndex" ));
    data = new Hex( properties.getProperty( "Data" ));
    notes = properties.getProperty( "Notes" );
  }

  /**
   * Store.
   * 
   * @param pw the pw
   */
  public void store( PropertyWriter pw )
  {
    pw.print( "KeyCode", keyCode );
    pw.print( "DeviceButtonIndex", deviceButtonIndex );
    pw.print( "Data", data );
    if (( notes != null ) && !notes.equals( "" ));
      pw.print( "Notes", notes );
  }

  /** The key code. */
  private int keyCode;
  
  /**
   * Gets the key code.
   * 
   * @return the key code
   */
  public int getKeyCode(){ return keyCode; }
  
  /**
   * Sets the key code.
   * 
   * @param code the new key code
   */
  public void setKeyCode( int code ){ keyCode = code; }

  /** The device button index. */
  private int deviceButtonIndex;
  
  /**
   * Gets the device button index.
   * 
   * @return the device button index
   */
  public int getDeviceButtonIndex(){ return deviceButtonIndex; }
  
  /**
   * Sets the device button index.
   * 
   * @param newIndex the new device button index
   */
  public void setDeviceButtonIndex( int newIndex )
  {
    deviceButtonIndex = newIndex;
  }

  /** The data. */
  private Hex data = null;
  
  /**
   * Gets the data.
   * 
   * @return the data
   */
  public Hex getData(){ return data; }
  
  /**
   * Sets the data.
   * 
   * @param hex the new data
   */
  public void setData( Hex hex ){ data = hex; }

  /** The notes. */
  private String notes = null;
  
  /**
   * Gets the notes.
   * 
   * @return the notes
   */
  public String getNotes(){ return notes; }
  
  /**
   * Sets the notes.
   * 
   * @param text the new notes
   */
  public void setNotes( String text ){ notes = text; }

  /** The unpack learned. */
  private UnpackLearned unpackLearned = null;
  
  /**
   * Gets the unpack learned.
   * 
   * @return the unpack learned
   */
  public UnpackLearned getUnpackLearned()
  {
    if ( unpackLearned == null )
      unpackLearned = new UnpackLearned( data );
    return unpackLearned;
  }

  /** The decodes. */
  private ArrayList< LearnedSignalDecode > decodes = null;
  
  /**
   * Gets the decodes.
   * 
   * @return the decodes
   */
  public ArrayList< LearnedSignalDecode > getDecodes()
  {
    if ( decodes == null )
    {
      UnpackLearned ul = getUnpackLearned();
      if ( !ul.ok )
        return null;
      getDecodeIR();
      decodeIR.setBursts( ul.durations, ul.repeat );
      decodeIR.setFrequency( ul.frequency );
      decodeIR.initDecoder();
      decodes = new ArrayList< LearnedSignalDecode >();
      while ( decodeIR.decode())
        decodes.add( new LearnedSignalDecode( decodeIR ));
    }
    return decodes;
  }

  /**
   * Gets the decode ir.
   * 
   * @return the decode ir
   */
  public static DecodeIRCaller getDecodeIR()
  {
    if ( decodeIR == null )
      decodeIR = new DecodeIRCaller( new File( System.getProperty( "user.dir" )));

    return decodeIR;
  }

  /** The decode ir. */
  private static DecodeIRCaller decodeIR = null;
}
