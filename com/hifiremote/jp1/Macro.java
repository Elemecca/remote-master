package com.hifiremote.jp1;

import java.util.*;

public class Macro
  extends AdvancedCode
{
  public Macro( int keyCode, Hex keyCodes, String notes )
  {
    super( keyCode, keyCodes, notes );
  }

  public Object getValue()
  {
    return getData();
  }

  public void setValue( Object value )
  {
    setData(( Hex )value );
  }
}
