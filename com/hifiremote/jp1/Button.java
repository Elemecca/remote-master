package com.hifiremote.jp1;

public class Button
{
  public Button( String standardName, String name, byte code )
  {
    this.standardName = standardName;
    this.name = name;
    keyCode = code;
    multiMacroAddress = 0;
  }

  public String toString(){ return name; }
  public String getName(){ return name; }
  public String getStandardName(){ return standardName; }
  public byte getKeyCode(){ return keyCode; }
  public int getMultiMacroAddress(){ return multiMacroAddress; }
  public void setMultiMacroAddress( int addr ){ multiMacroAddress = addr; }

  public Button setFunction( Function newFunc )
  {
    if ( function != null )
      function.removeReference();
    function = newFunc;
    if ( newFunc != null )
      newFunc.addReference();
    return this;
  }
  public Function getFunction(){ return function; }

  public Button setShiftedFunction( Function newFunc )
  {
    if ( shiftedFunction != null )
      shiftedFunction.removeReference();
    shiftedFunction = newFunc;
    if ( newFunc != null )
      newFunc.addReference();
    return this;
  }
  public Function getShiftedFunction(){ return shiftedFunction; }


  public byte[] getKeyMoves( byte[] deviceCode, DeviceType devType, Remote remote )
  {
    byte[] move1 = getKeyMove( function, false, deviceCode, devType, remote );
    byte[] move2 = getKeyMove( shiftedFunction, true, deviceCode, devType, remote );

    byte[] rc = new byte[ move1.length + move2.length ];

    System.arraycopy( move1, 0, rc, 0, move1.length );
    System.arraycopy( move2, 0, rc, move1.length, move2.length );

    return rc;
  }

  public byte[] getKeyMove( Function f, boolean shifted,
                            byte[] deviceCode, DeviceType devType, Remote remote )
  {
    byte[] rc = new byte[ 0 ];
    if (( f != null ) && ( f.getHex() != null ))
    {
      int len = 0;
      Hex hex = f.getHex();
      if ( f.isExternal())
      {
        ExternalFunction ef = ( ExternalFunction )f;
        devType = remote.getDeviceTypeByAliasName( ef.getDeviceTypeAliasName());
        int temp = devType.getNumber() * 0x1000 +
                   ef.getSetupCode() - remote.getDeviceCodeOffset();

        deviceCode = new byte[ 2 ];
        deviceCode[ 0 ] = ( byte )( temp >> 8 );
        deviceCode[ 1 ] = ( byte )temp;
      }

      if  ( f.isExternal() || shifted || !devType.isMapped( this ) )
        len = ( 4 + hex.length());

      rc = new byte[ len ];

      if ( len != 0 )
      {

        rc[ 0 ] = keyCode;
        if ( shifted )
          rc[ 0 ] = ( byte )( rc[ 0 ] | 0x80 );
        
        rc[ 1 ] = ( byte )( 0xF2 + hex.length() );
        System.arraycopy( deviceCode, 0, rc, 2, 2 );
        System.arraycopy( hex.getData(), 0, rc, 4, hex.length() );
      }
    }
    return rc;
  }

  private String name;
  private String standardName;
  private byte keyCode;
  private int multiMacroAddress;
  private Function function;
  private Function shiftedFunction;
  private boolean[] inMap = null;
}
