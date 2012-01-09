package com.hifiremote.jp1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.hifiremote.decodeir.DecodeIRCaller;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignal.
 */
public class LearnedSignal extends Highlight
{

  /**
   * Instantiates a new learned signal.
   * 
   * @param keyCode
   *          the key code
   * @param deviceButtonIndex
   *          the device button index
   * @param data
   *          the data
   * @param notes
   *          the notes
   */
  public LearnedSignal( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    this.keyCode = keyCode;
    this.deviceButtonIndex = deviceButtonIndex;
    this.data = data;
    this.notes = notes;
  }

  public LearnedSignal( LearnedSignal signal )
  {
    keyCode = signal.keyCode;
    deviceButtonIndex = signal.deviceButtonIndex;
    data = new Hex( signal.data );
    notes = signal.notes;

    unpackLearned = signal.unpackLearned;
    if ( signal.decodes != null )
    {
      List< LearnedSignalDecode > signalDecodes = signal.getDecodes();
      decodes = new ArrayList< LearnedSignalDecode >( signalDecodes.size() );
      for ( LearnedSignalDecode decode : signalDecodes )
      {
        decodes.add( new LearnedSignalDecode( decode ) );
      }
    }
  }

  public static LearnedSignal read( HexReader reader, Remote remote )
  {
    if ( reader.peek() == remote.getSectionTerminator() )
    {
      return null;
    }
    if ( reader.available() < 4 )
    {
      return null;
    }
    int keyCode = reader.read();
    int type = reader.read();
    int deviceButtonIndex = 0;
    if ( remote.getLearnedDevBtnSwapped() )
    {
      deviceButtonIndex = type & 0x0F;
    }
    else
    {
      deviceButtonIndex = type >> 4;
    }
    int length = reader.read();
    short[] data = reader.read( length );

    return new LearnedSignal( keyCode, deviceButtonIndex, new Hex( data ), null );
  }

  /**
   * Instantiates a new learned signal.
   * 
   * @param properties
   *          the properties
   */
  public LearnedSignal( Properties properties )
  {
    super( properties );
    keyCode = Integer.parseInt( properties.getProperty( "KeyCode" ) );
    deviceButtonIndex = Integer.parseInt( properties.getProperty( "DeviceButtonIndex" ) );
    data = new Hex( properties.getProperty( "Data" ) );
    notes = properties.getProperty( "Notes" );
  }
  
  public Hex getSignalHex( Remote remote )
  {
    // Get the signal complete with header
    short[] signal = new short[ getSize() ];
    Hex.put(data, signal, 3 );
    signal[ 0 ] = ( short )keyCode;
    if ( remote.getLearnedDevBtnSwapped() )
    {
      signal[ 1 ] = ( short )( 0x20 | deviceButtonIndex );
    }
    else
    {
      signal[ 1 ] = ( short )( deviceButtonIndex << 4 | 0x02 );
    }
    signal[ 2 ] = ( short )data.length();
    return new Hex( signal );
  }

  public int getSize()
  {
    return data.length() + 3; // keyCode, deviceButtonIndex, length, data
  }

  /**
   * Store.
   * 
   * @param pw
   *          the pw
   */
  public void store( PropertyWriter pw )
  {
    super.store( pw );
    pw.print( "KeyCode", keyCode );
    pw.print( "DeviceButtonIndex", deviceButtonIndex );
    pw.print( "Data", data );
    if ( notes != null && !notes.equals( "" ) )
    {
      pw.print( "Notes", notes );
    }
  }

  /** The key code. */
  private int keyCode;

  /**
   * Gets the key code.
   * 
   * @return the key code
   */
  public int getKeyCode()
  {
    return keyCode;
  }

  /**
   * Sets the key code.
   * 
   * @param code
   *          the new key code
   */
  public void setKeyCode( int code )
  {
    keyCode = code;
  }
  
  /** The device button index. */
  private int deviceButtonIndex;

  /**
   * Gets the device button index.
   * 
   * @return the device button index
   */
  public int getDeviceButtonIndex()
  {
    return deviceButtonIndex;
  }

  /**
   * Sets the device button index.
   * 
   * @param newIndex
   *          the new device button index
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
  public Hex getData()
  {
    return data;
  }

  /**
   * Sets the data.
   * 
   * @param hex
   *          the new data
   */
  public void setData( Hex hex )
  {
    data = hex;
  }

  /** The notes. */
  private String notes = null;

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

  public int store( short[] buffer, int offset, Remote remote )
  {
    buffer[ offset++ ] = ( short )keyCode;
    if ( remote.getLearnedDevBtnSwapped() )
    {
      buffer[ offset ] = ( short )( 0xFF & ( deviceButtonIndex | 0x20 ) );
    }
    else
    {
      buffer[ offset ] = ( short )( 0xFF & ( deviceButtonIndex << 4 | 2 ) );
    }
    ++offset;
    int dataLength = data.length();
    buffer[ offset++ ] = ( short )dataLength;
    Hex.put( data, buffer, offset );

    return offset + dataLength;
  }

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
    {
      unpackLearned = new UnpackLearned( data );
    }
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
      {
        return null;
      }
      getDecodeIR();
      decodeIR.setBursts( ul.durations, ul.repeat, ul.extra );
      decodeIR.setFrequency( ul.frequency );
      decodeIR.initDecoder();
      decodes = new ArrayList< LearnedSignalDecode >();
      while ( decodeIR.decode() )
      {
        decodes.add( new LearnedSignalDecode( decodeIR ) );
      }
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
    {
      decodeIR = new DecodeIRCaller( new File( System.getProperty( "user.dir" ) ) );
    }

    return decodeIR;
  }

  /** The decode ir. */
  private static DecodeIRCaller decodeIR = null;
}
