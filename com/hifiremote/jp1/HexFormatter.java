package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.text.*;

public class HexFormatter
  extends JFormattedTextField.AbstractFormatter
{
  HexFormatter()
  {
    super();
    filter = new HexFilter();
  }

  protected DocumentFilter getDocumentFilter()
  {
    return filter;
  }

  public Object stringToValue( String s )
  {
    return new Hex( s );
  }

  public String valueToString( Object o )
  {
    if ( o == null )
      return null;
    return (( Hex )o).toString();
  }

  private boolean isValid( String string )
  {
    for ( int i = 0; i < string.length(); i++ )
    {
      char ch = string.charAt( i );
      if ( !Character.isSpaceChar( ch ) && ( Character.digit( ch, 16 ) == -1 ))
      {
        invalidEdit();
        return false;
      }
    }
    return true;
  }

  public class HexFilter
    extends DocumentFilter
  {
    public void insertString( DocumentFilter.FilterBypass fb,
                              int offset,
                              String string,
                              AttributeSet attr )
      throws BadLocationException
    {
      if ( isValid( string ))
        super.insertString( fb, offset, string, attr );
    }

    public void replace( DocumentFilter.FilterBypass fb,
                         int offset, int length,
                         String string,
                         AttributeSet attr )
      throws BadLocationException
    {
      if ( isValid( string ))
        super.replace( fb, offset, length, string, attr );
    }
  }

  private HexFilter filter;
}
