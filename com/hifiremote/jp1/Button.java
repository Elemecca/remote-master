package com.hifiremote.jp1;

import java.awt.Shape;

public class Button
{
  public Button( String standardName, String name, int code )
  {
    this.standardName = standardName.toLowerCase();
    this.name = name;
    keyCode = code;
    multiMacroAddress = 0;
    if (( code & 0x80 ) != 0 )
      isShifted = true;
  }

  public String toString(){ return name; }
  public String getName(){ return name; }
  public void setName( String name ){ this.name = name; }
  public String getShiftedName()
  {
    if ( isShifted )
      return name;
    else if ( shiftedButton != null )
      return shiftedButton.getName();
    else
      return null;
  }
  public String getXShiftedName()
  {
    if ( isXShifted )
      return name;
    else if ( xShiftedButton != null )
      return xShiftedButton.getName();
    else
      return null;
  }
  public String getStandardName(){ return standardName; }
  public void setStandardName( String name ){ standardName = name.toLowerCase(); }
  public int getKeyCode(){ return keyCode; }
  public int getMultiMacroAddress(){ return multiMacroAddress; }
  public void setMultiMacroAddress( int addr ){ multiMacroAddress = addr; }

  public void setBaseButton( Button button )
  {
    System.err.println( "Button '" + name + "' setting baseButton to '" + button + "'" );
    baseButton = button;
  }

  public Button getBaseButton()
  {
    return baseButton;
  }

  public Button getShiftedButton()
  {
    return shiftedButton;
  }

  public void setShiftedButton( Button button )
  {
    System.err.println( "Button '" + name + "' setting shiftedButton to '" + button + "'" );
    shiftedButton = button;
  }

  public void setXShiftedButton( Button button )
  {
    System.err.println( "Button '" + name + "' setting xShiftedButton to '" + button + "'" );
    xShiftedButton = button;
  }
  public Button getXShiftedButton()
  {
    return xShiftedButton;
  }

  public boolean getIsNormal()
  {
    return (!isShifted && !isXShifted );
  }
  public void setIsShifted( boolean flag )
  {
    isShifted = flag;
    if ( isShifted )
      restrictions |= ( SHIFT | XSHIFT );
  }
  public boolean getIsShifted()
  {
    return isShifted;
  }

  public void setIsXShifted( boolean flag )
  {
    isXShifted = flag;
    if ( isXShifted )
      restrictions |= ( SHIFT | XSHIFT );
  }
  public boolean getIsXShifted(){ return isXShifted; }

  public int getRestrictions(){ return restrictions; }
  public void setRestrictions( int restrictions )
  {
    this.restrictions = restrictions;
  }
  public void addRestrictions( int restrictions )
  {
    this.restrictions  |= restrictions;
  }

  public boolean allowsKeyMove()
  {
    return (( restrictions & MOVE_BIND ) == 0 );
  }

  public boolean allowsShiftedKeyMove()
  {
    return (( restrictions & SHIFT_MOVE_BIND ) == 0 );
  }

  public boolean allowsXShiftedKeyMove()
  {
    return (( restrictions & XSHIFT_MOVE_BIND ) == 0 );
  }

  public Button setFunction( Function newFunc )
  {
    if ( function != null )
    {
      function.removeReference();
    }
    function = newFunc;
    if ( newFunc != null )
    {
      newFunc.addReference();
    }
    return this;
  }
  public Function getFunction(){ return function; }

  public Button setShiftedFunction( Function newFunc )
  {
    if ( shiftedButton != null )
      shiftedButton.setFunction( newFunc );
    else
    {
      if ( shiftedFunction != null )
        shiftedFunction.removeReference();
      shiftedFunction = newFunc;
      if ( newFunc != null )
        newFunc.addReference();
    }
    return this;
  }

  public Function getShiftedFunction()
  {
    if ( shiftedButton != null )
      return shiftedButton.getFunction();
    else
      return shiftedFunction;
  }

  public Button setXShiftedFunction( Function newFunc )
  {
    if ( xShiftedButton != null )
      xShiftedButton.setFunction( newFunc );
    else
    {
      if ( xShiftedFunction != null )
        xShiftedFunction.removeReference();
      xShiftedFunction = newFunc;
      if ( newFunc != null )
        newFunc.addReference();
    }
    return this;
  }

  public Function getXShiftedFunction()
  {
    if ( xShiftedButton != null )
      return xShiftedButton.getFunction();
    return xShiftedFunction;
  }

  public void addButtonMap( int mapIndex )
  {
    buttonMaps |= ( 1 << mapIndex );
  }

  public boolean inButtonMap( int mapIndex )
  {
    int mask = ( 1 << mapIndex );
    return (( buttonMaps & mask ) != 0 );
  }

  public int getButtonMaps()
  {
    return buttonMaps;
  }

  public int[] getKeyMoves( int[] deviceCode, DeviceType devType, Remote remote )
  {
    int[] move1 = getKeyMove( function, 0, deviceCode, devType, remote );
    int[] move2 = getKeyMove( shiftedFunction, remote.getShiftMask(), deviceCode, devType, remote );
    int[] move3 = getKeyMove( xShiftedFunction, remote.getXShiftMask(), deviceCode, devType, remote );

    int[] rc = new int[ move1.length + move2.length + move3.length ];

    System.arraycopy( move1, 0, rc, 0, move1.length );
    System.arraycopy( move2, 0, rc, move1.length, move2.length );
    System.arraycopy( move3, 0, rc, move1.length + move2.length, move3.length );

    return rc;
  }

  public int[] getKeyMove( Function f, int mask,
                            int[] deviceCode, DeviceType devType, Remote remote )
  {
    int[] rc = new int[ 0 ];
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

        deviceCode = new int[ 2 ];
        deviceCode[ 0 ] = ( temp >> 8 );
        deviceCode[ 1 ] = temp;
      }
      else if ( remote.getAdvCodeFormat() == Remote.EFC )
      {
        if ( hex.length() == 1 )
        {
          int[] data = new int[ 2 ];
          data[ 0 ] = 0;
          EFC efc = Protocol.hex2efc( hex, 0 );
          data[ 1 ] = efc.getValue();
          hex = new Hex( data );
        }
        else
        {
          int[] data = hex.getData();
          int[] newData = new int[ 2 ];

          newData[ 0 ] = (( data[ 0 ] & 0x1F ) << 3 ) |
                         (( data[ 0 ] & 0xE0 ) >> 5 );
          newData[ 0 ] ^= 0xAE;
          newData[ 0 ] += 156;
          newData[ 0 ] &= 0xFF;

          newData[ 1 ] = data[ 1 ] ^ 0xC5;

          hex = new Hex( newData );
        }
      }

      if  ( f.isExternal() || ( mask != 0 ) || !devType.isMapped( this ) )
        len = ( 4 + hex.length());

      rc = new int[ len ];

      if ( len != 0 )
      {

        rc[ 0 ] = keyCode;
        if ( mask != 0 )
          rc[ 0 ] = ( rc[ 0 ] | mask );

        rc[ 1 ] = ( 0xF2 + hex.length() );
        System.arraycopy( deviceCode, 0, rc, 2, 2 );
        System.arraycopy( hex.getData(), 0, rc, 4, hex.length() );
      }
    }
    return rc;
  }

  private String name;
  private String standardName;
  private int keyCode;
  private int multiMacroAddress;
  private Function function;
  private Function shiftedFunction;
  private Function xShiftedFunction;
  private boolean[] inMap = null;
  private Button baseButton = null;
  private Button shiftedButton = null;
  private Button xShiftedButton = null;
  private boolean isShifted = false;
  private boolean isXShifted = false;
  private int restrictions = 0;
  private int buttonMaps = 0;

  public static int MOVE_BIND = 0x01;
  public static int SHIFT_MOVE_BIND = 0x02;
  public static int XSHIFT_MOVE_BIND = 0x04;
  public static int ALL_MOVE_BIND = MOVE_BIND | SHIFT_MOVE_BIND + XSHIFT_MOVE_BIND;
  public static int MACRO_BIND = 0x08;
  public static int SHIFT_MACRO_BIND = 0x10;
  public static int XSHIFT_MACRO_BIND = 0x20;
  public static int ALL_MACRO_BIND = MACRO_BIND | SHIFT_MACRO_BIND | XSHIFT_MACRO_BIND;
  public static int LEARN_BIND = 0x40;
  public static int SHIFT_LEARN_BIND = 0x80;
  public static int XSHIFT_LEARN_BIND = 0x100;
  public static int ALL_LEARN_BIND = LEARN_BIND | SHIFT_LEARN_BIND | XSHIFT_LEARN_BIND;
  public static int MACRO_DATA = 0x200;
  public static int SHIFT_MACRO_DATA = 0x400;
  public static int XSHIFT_MACRO_DATA = 0x800;
  public static int ALL_MACRO_DATA = MACRO_DATA | SHIFT_MACRO_DATA | XSHIFT_MACRO_DATA;
  public static int TMACRO_DATA = 0x1000;
  public static int SHIFT_TMACRO_DATA = 0x2000;
  public static int XSHIFT_TMACRO_DATA = 0x4000;
  public static int ALL_TMACRO_DATA = TMACRO_DATA | SHIFT_TMACRO_DATA | XSHIFT_TMACRO_DATA;
  public static int FAV_DATA = 0x8000;
  public static int SHIFT_FAV_DATA = 0x10000;
  public static int XSHIFT_FAV_DATA = 0x20000;
  public static int ALL_FAV_DATA = FAV_DATA | SHIFT_FAV_DATA | XSHIFT_FAV_DATA;
  public static int BIND = MOVE_BIND | MACRO_BIND | LEARN_BIND;
  public static int SHIFT_BIND = SHIFT_MOVE_BIND | SHIFT_MACRO_BIND | SHIFT_LEARN_BIND;
  public static int XSHIFT_BIND = XSHIFT_MOVE_BIND | XSHIFT_MACRO_BIND | XSHIFT_LEARN_BIND;
  public static int ALL_BIND = ALL_MOVE_BIND | ALL_MACRO_BIND | ALL_LEARN_BIND;
  public static int DATA = MACRO_DATA | TMACRO_DATA | FAV_DATA;
  public static int SHIFT_DATA = SHIFT_MACRO_DATA | SHIFT_TMACRO_DATA | SHIFT_FAV_DATA;
  public static int XSHIFT_DATA = XSHIFT_MACRO_DATA | XSHIFT_TMACRO_DATA | XSHIFT_FAV_DATA;
  public static int ALL_DATA = ALL_MACRO_DATA | ALL_TMACRO_DATA | ALL_FAV_DATA;
  public static int SHIFT = SHIFT_BIND | SHIFT_DATA;
  public static int XSHIFT = XSHIFT_BIND | XSHIFT_DATA;
  public static int ALL = ALL_DATA | ALL_BIND;
}
