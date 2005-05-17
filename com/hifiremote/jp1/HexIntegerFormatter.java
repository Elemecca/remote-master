package com.hifiremote.jp1;

import java.text.*;
import javax.swing.text.*;

public class HexIntegerFormatter
  extends RegexFormatter
{
  public HexIntegerFormatter( int bits )
  {
    super();
    setValueClass( HexInteger.class );
    setBits( bits );
    setCommitsOnValidEdit( true );
  }

  public void setBits( int bits )
  {
    String textPattern = null;
    if ( bits < 5 )
      textPattern = patterns[ bits - 1 ];
    else if ( bits < 9 )
      textPattern = patterns[ bits - 5 ] + '?' + hexDigitPattern;
    else if ( bits < 13 )
      textPattern = patterns[ bits - 9 ] + '?' + hexDigitPattern + "{1,2}";
    else 
      textPattern = patterns[ bits - 13 ] + '?' + hexDigitPattern + "{1,3}";
    setPattern( textPattern );
  }

  private String hexDigitPattern = "\\p{XDigit}";

  private String[] patterns = 
  {
    "[01]",
    "[0-3]",
    "[0-7]",
    hexDigitPattern
  };
}
