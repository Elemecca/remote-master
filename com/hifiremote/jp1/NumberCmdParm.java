package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class NumberCmdParm
  extends CmdParameter
{
  public NumberCmdParm( String name, DefaultValue defaultValue )
  {
    this( name, defaultValue, 8 );
  }

  public NumberCmdParm( String name, DefaultValue defaultValue, int bits )
  {
    this( name, defaultValue, bits, 10 );
  }

  public NumberCmdParm( String name, DefaultValue defaultValue, int bits, int base )
  {
    super( name, defaultValue );
    this.bits = bits;
    this.base = base;
    editor = new ByteEditor(  0, (( 1 << bits ) - 1 ), this );
    editor.setBase( base );
    renderer = new ByteRenderer();
    renderer.setBase( base );
  }

  public String getDescription(){ return "Number"; }

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
    return Integer.class;
  }

  public void setValue( Object value )
  {
    this.value = value;
  }


  public Object getValue()
  {
    return value;
  }

  public Object convertValue( Object value )
  {
    Object rc = null;
    Class c = value.getClass();
    System.err.println( "NumberCmdParm.convertValue(): class is " + c );
    if (( c == Integer.class ) || ( c == Short.class ))
      rc = value;
    else // assume String
      rc = Integer.valueOf(( String )value, base );
    return rc;
  }

  public String toString()
  {
    StringBuilder buff = new StringBuilder();
    buff.append( name );
    if (( bits != 8 ) || ( base != 10 ))
    {
      buff.append( ':' );
      if ( base != 10 )
        buff.append( '$' );
      if ( bits != 8 )
        buff.append( bits );
    }
    if ( defaultValue != null )
    {
      buff.append( '=' );
      buff.append( defaultValue );
    }
    return buff.toString();
  }

  public void setBase( int base )
  {
    this.base = base;
    editor.setBase( base );
    renderer.setBase( base );
  }
  
  public int getBase(){ return base; }

  private ByteEditor editor;
  private ByteRenderer renderer;
  private int bits = 8;
  private int base = 10;
  private Object value = null;
}
