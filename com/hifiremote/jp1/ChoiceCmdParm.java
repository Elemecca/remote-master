package com.hifiremote.jp1;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ChoiceCmdParm
  extends CmdParameter
{
  public ChoiceCmdParm( String name, DefaultValue defaultValue, String[] textChoices )
  {
    super( name, defaultValue );
    choices = new Choice[ textChoices.length ];
    for ( int i = 0; i < choices.length; i++ )
    {
      choices[ i ] = new Choice( i, textChoices[ i ] );
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
    return choices[ (( Integer )val ).intValue()];
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
