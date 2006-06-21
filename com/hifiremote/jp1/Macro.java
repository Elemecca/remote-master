package com.hifiremote.jp1;

import java.util.Properties;

public class Macro
  extends AdvancedCode
{
  public Macro( int keyCode, Hex keyCodes, String notes )
  {
    super( keyCode, keyCodes, notes );
  }
  
  public Macro( Properties props )
  {
    super( props );
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
