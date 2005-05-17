package com.hifiremote.jp1;

import java.text.*;
import javax.swing.text.*;

public class IntegerFormatter
  extends NumberFormatter
{
  public IntegerFormatter( int bits )
  {
    super( new DecimalFormat());
    DecimalFormat format = ( DecimalFormat )getFormat();
    format.setParseIntegerOnly( true );
    format.setGroupingUsed( false );
    setValueClass( Integer.class );
    setMinimum( new Integer( 0 ));
    setBits ( bits );
    setCommitsOnValidEdit( true );
  }

  public void setBits( int bits )
  {
    int max = ( 2 << ( bits - 1 )) - 1;
    System.err.println( "Max integer is " + max );
    setMaximum( new Integer( max ));
  }
}
