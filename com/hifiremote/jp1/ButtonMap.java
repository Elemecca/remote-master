package com.hifiremote.jp1;

import java.util.Vector;

public class ButtonMap
{
  public ButtonMap( int num, byte[][] keyCodes )
  {
    number = num;
    keyCodeList = keyCodes;
  }

  public int getNumber(){ return number; }
  public byte[][] getKeyCodeList(){ return keyCodeList; }
  public ButtonMap setButtons( Button[] remoteButtons )
  {
    size = 0;

    buttons = new Button[ keyCodeList.length ][];

    for ( int i = 0; i < keyCodeList.length; i++ )
    {
      byte[] keyCodes = keyCodeList[ i ];
      Button[] inner = new Button[ keyCodes.length ];
      buttons[ i ] = inner;
      size += keyCodes.length;
      for ( int j = 0; j < keyCodes.length; j++ )
      {
        int keyCode = keyCodes[ j ];
        for ( int bi = 0; bi < remoteButtons.length ; bi++ )
        {
          Button button = remoteButtons[ bi ];
          if ( button.getKeyCode() == keyCode )
          {
            inner[ j ] = button;
            break;
          }
        }
        if ( inner[ j ] == null )
          System.err.println( "ERROR: ButtonMap " + number + " includes the keycode $" + 
                              Integer.toHexString( keyCode & 0xFF ) + 
                              ", but there is no matching button." );
      }
    }
    return this;
  }

  public boolean isPresent( Button b )
  {
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button[] inner = buttons[ i ];
      for ( int j = 0; j < inner.length; j++ )
      {
        if ( inner[ j ] == b )
        {
          return true;
        }
      }
    }
    return false;
  }

  public Button get( int index )
  {
    Button rc = null;
    int offset = 0;
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button[] inner = buttons[ i ];
      if ( index  < ( offset + inner.length ))
      {
        rc = inner[ index - offset ];
        break;
      }
      else
        offset += inner.length;
    }
    return rc;
  }

  public int size(){ return size; }

  public byte[] toBitMap( boolean digitMapUsed )
  {
    int len = ( buttons.length + 6 )/ 7;
    if ( len == 0 )
      return new byte[ 0 ];
    byte[] rc = new byte[ len ];
    int index = 0;
    int temp = 0x80;
    int limit = 0;
    for ( int i = 0 ; i < buttons.length; i++ )
    {
      Button[] inner = buttons[ i ];
      for ( int j = 0; j < inner.length; j++ )
      {
        Function func = inner[ j ].getFunction();
        if ( digitMapUsed && ( i == 0 ))
          func = null;
        if (( func != null ) &&
             !func.isExternal() &&
            ( func.getHex() != null ))
        {
          rc[index] |= temp;
          limit = index;
          break;
        }
      }

      temp >>= 1;
      if (temp == 1)
      {
        temp = 0x80;
        ++index;
      }
    }
    rc[limit++] |= 1;
    byte[] result = new byte[limit];
    System.arraycopy(rc,0,result,0,limit);
    return result;
  }

  public byte[] toCommandList( boolean digitMapUsed )
  {
    int count = 0;
    int funcLen = 0;
    boolean[] flags = new boolean[ buttons.length ];
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button[] inner = buttons[ i ];
      flags[ i ] = false;
      for ( int j = 0; j < inner.length; j++ )
      {
        Function func = inner[ j ].getFunction();
        if ( digitMapUsed && ( i == 0 ))
          func = null;
        if (( func != null ) &&
            ( func.getClass() != ExternalFunction.class ) &&
            ( func.getHex() != null ))
        {
          funcLen = func.getHex().length();
          flags[ i ] = true;
          count += ( inner.length * funcLen );
          break;
        }
      }
    }
    byte[] rc = new byte[ count ];
    byte[] zeros = new byte[ funcLen ];
    int index = 0;
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button[] inner = buttons[ i ];
      if ( flags[ i ] )
      {
        for ( int j = 0; j < inner.length; j++ )
        {
          byte[] hex = null;
          Function func = inner[ j ].getFunction();
          if ( digitMapUsed && ( i == 0 ))
            func = null;
          if (( func == null ) ||
              ( func.getClass() == ExternalFunction.class ) ||
              ( func.getHex() == null ))
            hex = zeros;
          else
            hex = func.getHex().getData();
          System.arraycopy( hex, 0, rc, index, funcLen );
          index += funcLen;
        }
      }
    }
    return rc;
  }

  private int number;
  private int size = 0;
  private byte[][] keyCodeList;
  private Button[][] buttons;
}
