package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class Setting.
 */
public class Setting
{
  
  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle(){ return title; }
  
  /**
   * Gets the byte address.
   * 
   * @return the byte address
   */
  public int getByteAddress(){ return byteAddress; }
  
  /**
   * Gets the bit number.
   * 
   * @return the bit number
   */
  public int getBitNumber(){ return bitNumber; }
  
  /**
   * Gets the number of bits.
   * 
   * @return the number of bits
   */
  public int getNumberOfBits(){ return numberOfBits; }
  
  /**
   * Gets the initial value.
   * 
   * @return the initial value
   */
  public int getInitialValue(){ return initialValue; }
  
  /**
   * Checks if is inverted.
   * 
   * @return true, if is inverted
   */
  boolean isInverted(){ return inverted; }
  
  /**
   * Gets the options.
   * 
   * @param r the r
   * 
   * @return the options
   */
  public Object[] getOptions( Remote r )
  { 
    if ( optionList != null )
      return optionList;
    else if ( sectionName != null )
      return r.getSection( sectionName );
    
    return null;
  }

  /**
   * Instantiates a new setting.
   * 
   * @param name the name
   * @param byteAddr the byte addr
   * @param bitNum the bit num
   * @param numBits the num bits
   * @param initVal the init val
   * @param invert the invert
   * @param options the options
   * @param section the section
   */
  public Setting( String name, int byteAddr, int bitNum, int numBits,
                  int initVal, boolean invert, Object[] options,
                  String section )
  {
    title = name;
    byteAddress = byteAddr;
    bitNumber = bitNum;
    numberOfBits = numBits;
    initialValue = initVal;
    inverted = invert;
    optionList = options;
    sectionName = section;
    value = initVal;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 100 );
    temp.append( title )
        .append( "=$" ).append( Integer.toHexString( byteAddress ))
        .append( '.' ).append( bitNumber )
        .append( '.' ).append( numberOfBits )
        .append( '.' ).append( initialValue )
        .append( '.' ).append( inverted ? 1 : 0 );

    if ( optionList != null )
    {
      temp.append( " (" );
      for ( int i = 0; i < optionList.length; i++ )
      {
        if ( i > 0 )
          temp.append( ';' );
        temp.append( optionList[ i ]);
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
   * @param value the new value
   */
  public void setValue( int value )
  {
    this.value = value;
  }

  /**
   * Decode.
   * 
   * @param data the data
   */
  public void decode( short[] data )
  {
    int mask = ( 1 << numberOfBits ) - 1;
    int temp = data[ byteAddress ];
    if ( inverted )
      temp = ~temp;
    int shift = bitNumber - numberOfBits + 1;
    temp >>= shift;
    value = temp & mask;
  }

  /**
   * Store.
   * 
   * @param data the data
   */
  public void store( short[] data )
  {
    int val = value;
    int mask = ( 1 << numberOfBits ) - 1;
    if ( inverted )
      val = ~val;
    val &= mask;
     
    mask = ~mask & 0xFF;
    int temp = data[ byteAddress ] & mask;
    int shift = bitNumber - numberOfBits + 1;
    temp |= ( val << shift );
    data[ byteAddress ] = ( short )temp;
  }
  
  /**
   * Store.
   * 
   * @param pw the pw
   */
  public void store( PropertyWriter pw )
  {
    pw.print( title, value );
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
