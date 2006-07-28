package com.hifiremote.jp1;

public class Setting
{
  public String getTitle(){ return title; }
  public int getByteAddress(){ return byteAddress; }
  public int getBitNumber(){ return bitNumber; }
  public int getNumberOfBits(){ return numberOfBits; }
  public int getInitialValue(){ return initialValue; }
  boolean isInverted(){ return inverted; }
  public Object[] getOptions( Remote r )
  { 
    if ( optionList != null )
      return optionList;
    else if ( sectionName != null )
      return r.getSection( sectionName );
    
    return null;
  }

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

  public String toString()
  {
    StringBuffer temp = new StringBuffer( 100 );
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
  
  public int getValue()
  {
    return value;
  }
  
  public void setValue( int value )
  {
    this.value = value;
  }

  public void decode( short[] data )
  {
    int mask = ( 1 << numberOfBits ) - 1;
    int temp = data[ byteAddress ];
    if ( inverted )
      temp = ~temp;
    temp &= mask;
    int shift = bitNumber - numberOfBits + 1;
    temp >>= shift;
    value = temp;
  }

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
  
  public void store( PropertyWriter pw )
  {
    pw.print( title, value );
  }

  private String title;
  private int byteAddress;
  private int bitNumber;
  private int numberOfBits;
  private int initialValue;
  private boolean inverted;
  private Object[] optionList = null;
  private String sectionName = null;
  private int value = 0;
}
