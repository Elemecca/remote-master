package com.hifiremote.jp1;

import java.util.*;

public class ButtonMap
{
  public ButtonMap( int num, int[][] keyCodes )
  {
    number = num;
    keyCodeList = keyCodes;
  }

  public int getNumber(){ return number; }
  public int[][] getKeyCodeList(){ return keyCodeList; }
  public ButtonMap setButtons( Remote remote )
  {
    size = 0;

    buttons = new Button[ keyCodeList.length ][];

    for ( int i = 0; i < keyCodeList.length; i++ )
    {
      int[] keyCodes = keyCodeList[ i ];
      Button[] inner = new Button[ keyCodes.length ];
      buttons[ i ] = inner;
      size += keyCodes.length;
      for ( int j = 0; j < keyCodes.length; j++ )
      {
        int keyCode = keyCodes[ j ];
        Button button = remote.getButton( keyCode );
        if ( button == null )
        {
          System.err.println( "ERROR: ButtonMap " + number + " includes unknown keycode $" +
                              Integer.toHexString( keyCode & 0xFF ) +
                              ", Creating button!" );
          String name = "button" + Integer.toHexString( keyCode & 0xFF ).toUpperCase();
          button = new Button( name, name, keyCode, remote );
          remote.addButton( button );
        }
        button.addButtonMap( number );
        inner[ j ] = button;
      }
    }
    return this;
  }

  public boolean isPresent( Button b )
  {
    if ( b == null )
      return false;
    return b.inButtonMap( number );
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

  public Vector parseBitMap( int[] bitMap, int offset, boolean digitMapUsed )
  {
    Vector rc = new Vector();
    int count = 0;
    int mask = 0x80;
    for ( int i = 0; i < buttons.length; i++ )
    {
      boolean useIt = (( bitMap[ offset ] & mask ) != 0 ); 
      if ( useIt )
      {
        Button[] inner = buttons[ i ];
        for ( int j = 0; j < inner.length; j++ )
          rc.add( inner[ j ]);
      }
      mask >>= 1;
      if ( mask == 1 )
      {
        mask = 0x80;
        offset++;
      }
    }
    return rc;
  }

  public int[] toBitMap( boolean digitMapUsed )
  {
    int len = ( buttons.length + 6 )/ 7;
    if ( len == 0 )
      return new int[ 0 ];
    int[] rc = new int[ len ];
    int index = 0;
    int temp = 0x80;
    int limit = 0;
    for ( int i = 0 ; i < buttons.length; i++ )
    {
      Button[] inner = buttons[ i ];
      for ( int j = 0; j < inner.length; j++ )
      {
        Function func = null;
        if ( inner[ j ] != null )
          func = inner[ j ].getFunction();
        if ( digitMapUsed && ( i == 0 ))
          func = null;
        if (( func != null ) &&
             !func.isExternal() &&
            ( func.getHex() != null ))
        {
          rc[ index ] |= temp;
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
    rc[ limit++ ] |= 1;
    int[] result = new int[ limit ];
    System.arraycopy(rc,0,result,0,limit);
    return result;
  }

  public int[] toCommandList( boolean digitMapUsed )
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
    int[] rc = new int[ count ];
    int[] zeros = new int[ funcLen ];
    int index = 0;
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button[] inner = buttons[ i ];
      if ( flags[ i ] )
      {
        for ( int j = 0; j < inner.length; j++ )
        {
          int[] hex = null;
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
  private int[][] keyCodeList;
  private Button[][] buttons;
}
