package com.hifiremote.jp1;

import java.awt.Color;

// TODO: Auto-generated Javadoc
/**
 * The Class Setting.
 */
public class Setting extends Highlight
{
  public class NamedHex
  {
    private String name = null;
    private Hex hex = null;
    
    public NamedHex( String name, Hex hex )
    {
      this.name = name;
      this.hex = hex;
    }

    public String getName()
    {
      return name;
    }

    public Hex getHex()
    {
      return hex;
    }
    
    @Override
    public String toString()
    {
      return name;
    }
  }
  
  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * Gets the byte address.
   * 
   * @return the byte address
   */
  public int getByteAddress()
  {
    return byteAddress;
  }

  /**
   * Gets the bit number.
   * 
   * @return the bit number
   */
  public int getBitNumber()
  {
    return bitNumber;
  }

  /**
   * Gets the number of bits.
   * 
   * @return the number of bits
   */
  public int getNumberOfBits()
  {
    return numberOfBits;
  }

  /**
   * Gets the initial value.
   * 
   * @return the initial value
   */
  public int getInitialValue()
  {
    return initialValue;
  }

  /**
   * Checks if is inverted.
   * 
   * @return true, if is inverted
   */
  boolean isInverted()
  {
    return inverted;
  }

  /**
   * Gets the options.
   * 
   * @param r
   *          the r
   * @return the options
   */
  public Object[] getOptions( Remote r )
  {
    Object[] choices = null;
    if ( optionList != null )
    {  
      choices = optionList;
    }
    else if ( sectionName != null )
    {
      choices = r.getSection( sectionName );

      if ( r.getSoftDevices() != null
            && r.getSoftDevices().getAllowEmptyButtonSettings() 
            && sectionName.equals( "DeviceButtons" ) )
      {
        Object[] oldChoices = choices;
        int length = oldChoices.length;
        choices = new Object[ length + 1 ];
        System.arraycopy( oldChoices, 0, choices, 0, length );
        choices[ length ] = DeviceButton.noButton;
      }
    }
    return choices;
  }

  /**
   * Instantiates a new setting.
   * 
   * @param name
   *          the name
   * @param byteAddr
   *          the byte addr
   * @param bitNum
   *          the bit num
   * @param numBits
   *          the num bits
   * @param initVal
   *          the init val
   * @param invert
   *          the invert
   * @param options
   *          the options
   * @param section
   *          the section
   */
  public Setting( String name, int byteAddr, int bitNum, int numBits, int initVal, boolean invert, Object[] options,
      String section )
  {
    title = name;
    byteAddress = byteAddr;
    bitNumber = bitNum;
    numberOfBits = numBits;
    initialValue = initVal;
    inverted = invert; 
    sectionName = section;
    value = initVal;
    if ( numberOfBits == 0 )
    {
      NamedHex named[] = new NamedHex[ options.length ];
      for ( int i = 0; i < options.length; i++ )
      {
        String str = ( String )options[ i ];
        int pos = str.indexOf( ':' );
        if ( pos <= 0 || pos == str.length() - 1 )
        {
          System.err.println( "Illegal format of Named Hex option \"" + options[ i ] + "\"" );
          named[ i ] = null;
          continue;
        }
        String hexName = str.substring( 0, pos ).trim();
        Hex hexVal = new Hex( str.substring( pos + 1 ).trim() );
        named[ i ] = new NamedHex( hexName, hexVal );
      }
      optionList = named;
    }
    else
    {
      optionList = options;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 100 );
    temp.append( title ).append( "=$" ).append( Integer.toHexString( byteAddress ) ).append( '.' ).append( bitNumber )
        .append( '.' ).append( numberOfBits ).append( '.' ).append( initialValue ).append( '.' ).append(
            inverted ? 1 : 0 );

    if ( optionList != null )
    {
      temp.append( " (" );
      for ( int i = 0; i < optionList.length; i++ )
      {
        if ( i > 0 )
        {
          temp.append( ';' );
        }
        Object obj = optionList[ i ];
        if ( obj instanceof NamedHex )
        {
          NamedHex nh = ( NamedHex )obj;
          temp.append( nh.getName() + " : " + nh.getHex() );
        }
        else
        {
          temp.append( optionList[ i ] );
        }
      }
      temp.append( ')' );
    }
    else if ( sectionName != null )
      temp.append( ' ' ).append( sectionName );

    return temp.toString();
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public int getValue()
  {
    return value;
  }

  /**
   * Sets the value.
   * 
   * @param value
   *          the new value
   */
  public void setValue( int value )
  {
    this.value = value;
  }

  /**
   * Decode.
   * 
   * @param data
   *          the data
   */
  public void decode( short[] data, Remote remote )
  {
    int temp = data[ byteAddress ];
    if ( remote.getSoftDevices() != null 
        && remote.getSoftDevices().getAllowEmptyButtonSettings()
        && sectionName != null
        && sectionName.equals( "DeviceButtons" ) 
        && temp == 0xFF )
    {
      value = getOptions( remote ).length - 1;
      return;
    }
    if ( numberOfBits == 0 )
    {
      int i = 0;
      for ( ; i < optionList.length; i++ )
      {
        Hex hex = ( ( NamedHex )optionList[ i ] ).getHex();
        int size = hex.length();
        if ( Hex.subHex( data, byteAddress, size ).equals( hex )  )
        {
          value = i;
          return;
        }
      }
      value = initialValue;
      return;
    } 
    int mask = ( 1 << numberOfBits ) - 1;
    if ( inverted )
      temp = ~temp;
    int shift = bitNumber - numberOfBits + 1;
    temp >>= shift;
    value = temp & mask;
  }

  /**
   * Store.
   * 
   * @param data
   *          the data
   */
  public void store( short[] data, Remote remote )
  {
    if ( remote.getSoftDevices() != null 
        && remote.getSoftDevices().getAllowEmptyButtonSettings()
        && sectionName != null
        && sectionName.equals( "DeviceButtons" ) 
        && value == getOptions( remote ).length - 1 )
    {
      data[ byteAddress ] = 0xFF;
      return;
    }
    
    if ( numberOfBits == 0 )
    {
      NamedHex nh = ( NamedHex )( optionList[ value ] );
      Hex.put( nh.getHex().getData(), data, byteAddress );
      return;
    }
    
    int mask = ( 1 << numberOfBits ) - 1;
    int shift = bitNumber - numberOfBits + 1;
    int val = value;

    if ( inverted )
      val = ~val;
    val &= mask;

    val <<= shift;
    mask <<= shift;
    mask = ~mask;

    int temp = data[ byteAddress ];
    temp &= mask;
    temp |= val;
    data[ byteAddress ] = ( short )temp;
  }

  /**
   * Store.
   * 
   * @param pw
   *          the pw
   */
  public void store( PropertyWriter pw )
  {
    pw.print( title, value );
  }
  
  public void doHighlight( Color[] highlight, int index, Remote remote )
  {
    if ( optionList != null && optionList[ value ] instanceof NamedHex )
    {
      int byteAddress = remote.getSettingBytes().get( index );
      NamedHex nh = ( NamedHex ) optionList[ value ];
      int size = nh.getHex().length();
      for ( int i = 0; i < size; i++ )
      {
        highlight[ byteAddress + i ] = getHighlight();
      }
      setMemoryUsage( size );
      return;
    }

    int end = highlight.length - 1;
    for ( int i = 0; i < numberOfBits; i++ )
    {
      highlight[ end - 8 * index - bitNumber + i ] = getHighlight();
    }
    setMemoryUsage( - numberOfBits );
  }

  /** The title. */
  private String title;

  /** The byte address. */
  private int byteAddress;

  /** The bit number. */
  private int bitNumber;

  /** The number of bits. */
  private int numberOfBits;

  /** The initial value. */
  private int initialValue;

  /** The inverted. */
  private boolean inverted;

  /** The option list. */
  private Object[] optionList = null;

  /** The section name. */
  private String sectionName = null;

  /** The value. */
  private int value = 0;
 
}
