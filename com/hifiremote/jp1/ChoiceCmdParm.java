package com.hifiremote.jp1;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class ChoiceCmdParm
  extends CmdParameter
{
  public ChoiceCmdParm( String name, DefaultValue defaultValue, Vector textChoices )
  {
    super( name, defaultValue );
    System.err.println( "ChoiceCmdParm.ChoiceCmdParm()" );
    int numChoices = 0;
    for ( Enumeration e = textChoices.elements(); e.hasMoreElements(); )
    {
      String str = ( String )e.nextElement();
      System.err.println( "Got text choice " + str );
      if ( str != null )
        numChoices++;
    }
    System.err.println( "numChoices=" + numChoices );
    choices = new Choice[ numChoices ];
    int index = 0;
    int i = 0;
    for ( Enumeration e = textChoices.elements(); e.hasMoreElements(); )
    {
      String str = ( String )e.nextElement();
      if ( str != null )
      {
        System.err.println( "choices[" + i + "] is " + str );
        choices[ i++ ] = new Choice( index, str );
      }
      index++;
    }

    editor = new ChoiceEditor( choices );
    renderer = new ChoiceRenderer( choices );
    renderer.setHorizontalAlignment( SwingConstants.CENTER );
  }

  public TableCellEditor getEditor()
  {
    return editor;
  }

  public TableCellRenderer getRenderer()
  {
    return renderer;
  }

  public Class getValueClass()
  {
    return Choice.class;
  }

  public Object getValue()
  {
    return null;
  }

  public Object getValue( Object val )
  {
    int i = (( Integer )val ).intValue();
    for ( int j = 0; j < choices.length; j++ )
    {
      if ( choices[ j ].getIndex() == i )
        return ( choices[ j ]);
    }
    return null;
  }

  public Object convertValue( Object value )
  {
    Object rc = null;
    Class c = value.getClass();
    if ( c == Choice.class )
      rc = new Integer((( Choice )value ).getIndex());
    else if ( c == Integer.class )
      rc = value;
    else // assume String
    { 
      String str = ( String )value;
      for ( int i = 0; i < choices.length; i++ )
      {
        if ( str.equals( choices[ i ].getText()))
        {
          rc = new Integer( i );
          break;
        }
      }
    }
    return rc;
  }

  public void setValue( Object value )
  {
  }

  public Choice[] getChoices(){ return choices; }

  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    buff.append( name );
    buff.append( ':' );
    for ( int i = 0; i < choices.length; i ++ )
    {
      if ( i > 0 )
        buff.append( '|' );
      buff.append( choices[ i ].getText());
    }
    if ( defaultValue != null )
    {
      buff.append( '=' );
      buff.append( defaultValue ); 
    }
    return buff.toString();     
  }

  private DefaultCellEditor editor = null;
  private DefaultTableCellRenderer renderer = null;
  private Choice[] choices = null;
}
