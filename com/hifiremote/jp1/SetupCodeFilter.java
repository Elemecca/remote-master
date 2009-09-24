/**
 * 
 */
package com.hifiremote.jp1;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * @author Greg
 */
public class SetupCodeFilter extends DocumentFilter
{
  private JTextField tf;

  public SetupCodeFilter( JTextField tf )
  {
    this.tf = tf;
  }

  public void insertString( DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr )
      throws BadLocationException
  {

    if ( string == null )
    {
      return;
    }
    else
    {
      replace( fb, offset, 0, string, attr );
    }
  }

  public void remove( DocumentFilter.FilterBypass fb, int offset, int length ) throws BadLocationException
  {

    replace( fb, offset, length, "", null );
  }

  public void replace( DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs )
      throws BadLocationException
  {
    Document doc = fb.getDocument();
    int currentLength = doc.getLength();
    String currentContent = doc.getText( 0, currentLength );
    String before = currentContent.substring( 0, offset );
    String after = currentContent.substring( length + offset, currentLength );
    String newValue = before + ( text == null ? "" : text ) + after;
    checkInput( newValue, offset );
    fb.replace( offset, length, text, attrs );
  }

  private void checkInput( String proposedValue, int offset ) throws BadLocationException
  {
    if ( proposedValue.length() > 0 )
    {
      boolean isValid = false;
      try
      {
        int value = Integer.parseInt( proposedValue );
        isValid = ( value >= 0 && value <= SetupCode.getMax() );
      }
      catch ( NumberFormatException e )
      {
        isValid = false;
      }
      if ( !isValid )
      {
        Toolkit.getDefaultToolkit().beep();
        JP1Frame.showMessage( "Setup code must be a number between 0 and " + SetupCode.getMax(), tf );
        throw new BadLocationException( proposedValue, offset );
      }
      else
      {
        JP1Frame.clearMessage( tf );
      }
    }
  }
}