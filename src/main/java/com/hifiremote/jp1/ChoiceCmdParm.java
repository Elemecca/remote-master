package com.hifiremote.jp1;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ChoiceCmdParm.
 */
public class ChoiceCmdParm
  extends CmdParameter
{
  
  /**
   * Instantiates a new choice cmd parm.
   * 
   * @param name the name
   * @param defaultValue the default value
   * @param textChoices the text choices
   */
  public ChoiceCmdParm( String name, DefaultValue defaultValue, List< String > textChoices )
  {
    super( name, defaultValue );
    int numChoices = 0;
    for ( String str : textChoices )
    {
      if ( str != null )
        numChoices++;
    }
    choices = new Choice[ numChoices ];
    int index = 0;
    int i = 0;
    for ( String str : textChoices )
    {
      if ( str != null )
        choices[ i++ ] = new Choice( index, str );
      index++;
    }

    editor = new ChoiceEditor( choices );
    renderer = new ChoiceRenderer( choices );
    renderer.setHorizontalAlignment( SwingConstants.CENTER );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getDescription()
   */
  public String getDescription(){ return "Choice"; }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.CmdParameter#getEditor()
   */
  public TableCellEditor getEditor()
  {
    return editor;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.CmdParameter#getRenderer()
   */
  public TableCellRenderer getRenderer()
  {
    return renderer;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.CmdParameter#getValueClass()
   */
  public Class<?> getValueClass()
  {
    return Choice.class;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getValue()
   */
  public Object getValue()
  {
    return null;
  }

  /**
   * Gets the value.
   * 
   * @param val the val
   * 
   * @return the value
   */
  public Object getValue( Integer val )
  {
    int i = (( Integer )val ).intValue();
    for ( int j = 0; j < choices.length; j++ )
    {
      if ( choices[ j ].getIndex() == i )
        return ( choices[ j ]);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.CmdParameter#convertValue(java.lang.Object)
   */
  public Object convertValue( Object value )
  {
    Object rc = null;
    Class<?> c = value.getClass();
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

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#setValue(java.lang.Object)
   */
  public void setValue( Object value )
  {
  }

  /**
   * Gets the choices.
   * 
   * @return the choices
   */
  public Choice[] getChoices(){ return choices; }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder buff = new StringBuilder();
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

  /** The editor. */
  private DefaultCellEditor editor = null;
  
  /** The renderer. */
  private DefaultTableCellRenderer renderer = null;
  
  /** The choices. */
  private Choice[] choices = null;
}
