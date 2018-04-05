package com.hifiremote.jp1;

import java.text.*;
import javax.swing.text.*;

// TODO: Auto-generated Javadoc
/**
 * The Class IntegerFormatter.
 */
public class IntegerFormatter
  extends NumberFormatter
{
  
  /**
   * Instantiates a new integer formatter.
   * 
   * @param bits the bits
   */
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

  /**
   * Sets the bits.
   * 
   * @param bits the new bits
   */
  public void setBits( int bits )
  {
    int max = ( 2 << ( bits - 1 )) - 1;
    System.err.println( "Max integer is " + max );
    setMaximum( new Integer( max ));
  }
}
