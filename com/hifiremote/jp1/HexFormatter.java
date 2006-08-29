package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.text.*;

public class HexFormatter
  extends RegexFormatter
{
  HexFormatter( int length )
  {
    super();
    setValueClass( Hex.class );
    setAllowsInvalid( false );
    setOverwriteMode( true );
    setCommitsOnValidEdit( true );
    setLength( length );
  }

  public void setLength( int length )
  {
    StringBuilder buff = new StringBuilder();
    if ( length > 0 )
    {
      buff.append( "\\p{XDigit}{2}" );
      if ( length > 1 )
        buff.append( "( +\\p{XDigit}{2}){" + ( length - 1 ) + "}" );
    }
    setPattern( buff.toString());
  }
}
