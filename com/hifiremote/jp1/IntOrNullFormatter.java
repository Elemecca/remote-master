package com.hifiremote.jp1;

import java.text.ParseException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DocumentFilter;

public class IntOrNullFormatter
  extends DefaultFormatter
{
  public IntOrNullFormatter( int min, int max )
  {
    this.minVal = min;
    this.maxVal = max;
  }

  protected DocumentFilter getDocumentFilter()
  {
    if ( docFilter == null )
      docFilter = new DocumentFilter()
      {
        public void insertString(DocumentFilter.FilterBypass fb,
                                 int offset,
                                 String string,
                                 AttributeSet attrs )
          throws BadLocationException
        {
          try
          {
            char[] chars = string.toCharArray();
            for ( int i = 0; i < chars.length; i++ )
            {
              if ( !Character.isDigit( chars[ i ]))
              {
                invalidEdit();
                return;
              }
            }
            String text = getFormattedTextField().getText();
            StringBuilder buff = new StringBuilder( chars.length + string.length());
            buff.append( text.substring( 0, offset ));
            buff.append( string );
            buff.append( text.substring( offset ));

            int temp = Integer.parseInt( buff.toString());
            if (( temp < minVal ) || ( temp > maxVal ))
              invalidEdit();
            else
              fb.insertString( offset, string, attrs );
          }
          catch ( NumberFormatException e )
          {
            invalidEdit();
          }
        }

        public void replace(DocumentFilter.FilterBypass fb,
                            int offset,
                            int length,
                            String string,
                            AttributeSet attrs)
          throws BadLocationException
        {
          try
          {
            char[] chars = string.toCharArray();
            for ( int i = 0; i < chars.length; i++ )
            {
              if ( !Character.isDigit( chars[ i ]))
              {
                invalidEdit();
                return;
              }
            }
            String text = getFormattedTextField().getText();

            StringBuilder buff = new StringBuilder();
            buff.append( text.substring( 0, offset ));
            buff.append( string );
            buff.append( text.substring( offset + length ));

            int temp = Integer.parseInt( buff.toString());
            if (( temp < minVal ) || ( temp > maxVal ))
              invalidEdit();
            else
              fb.insertString( offset, string, attrs );
          }
          catch ( Exception e )
          {
            invalidEdit();
          }
        }
      };
    return docFilter;
  }

  public Class getValueClass()
  {
    return Integer.class;
  }

  public Object stringToValue( String string )
    throws ParseException
  {
    if (( string == null ) || ( string.length() == 0 ))
      return null;
    else
      return new Integer( string );
  }

  private int minVal = 0;
  private int maxVal = 0;
  private DocumentFilter docFilter = null;
}
