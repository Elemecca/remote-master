package com.hifiremote.jp1;

public class Setting
{
  public String getTitle(){ return title; }
  public int getByteAddress(){ return byteAddress; }
  public int getBitNumber(){ return bitNumber; }
  public int getNumberOfBits(){ return numberOfBits; }
  public int getInitialValue(){ return initialValue; }
  boolean isInverted(){ return inverted; }
  public String[] getOptionList(){ return optionList; }
  public String getSectionName(){ return sectionName; }

  public Setting( String name, int byteAddr, int bitNum, int numBits,
                  int initVal, boolean invert, String[] options,
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

  private String title;
  private int byteAddress;
  private int bitNumber;
  private int numberOfBits;
  private int initialValue;
  private boolean inverted;
  private String[] optionList = null;
  private String sectionName = null;
}
