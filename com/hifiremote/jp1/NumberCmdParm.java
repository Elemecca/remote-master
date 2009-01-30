package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class NumberCmdParm.
 */
public class NumberCmdParm
  extends CmdParameter
{
  
  /**
   * Instantiates a new number cmd parm.
   * 
   * @param name the name
   * @param defaultValue the default value
   */
  public NumberCmdParm( String name, DefaultValue defaultValue )
  {
    this( name, defaultValue, 8 );
  }

  /**
   * Instantiates a new number cmd parm.
   * 
   * @param name the name
   * @param defaultValue the default value
   * @param bits the bits
   */
  public NumberCmdParm( String name, DefaultValue defaultValue, int bits )
  {
    this( name, defaultValue, bits, 10 );
  }

  /**
   * Instantiates a new number cmd parm.
   * 
   * @param name the name
   * @param defaultValue the default value
   * @param bits the bits
   * @param base the base
   */
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

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getDescription()
   */
  public String getDescription(){ return "Number"; }

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
    return Integer.class;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#setValue(java.lang.Object)
   */
  public void setValue( Object value )
  {
    this.value = value;
  }


  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getValue()
   */
  public Object getValue()
  {
    return value;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.CmdParameter#convertValue(java.lang.Object)
   */
  public Object convertValue( Object value )
  {
    if ( value == null ) return null;

    Class<?> c = value.getClass();
    System.err.println( "NumberCmdParm.convertValue(): class is " + c );
    if (( c == Integer.class ) || ( c == Short.class ))
      return value;
    else // assume String
      return Integer.valueOf(( String )value, base );
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
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
  
  /**
   * Sets the bits.
   * 
   * @param bits the new bits
   */
  public void setBits( int bits )
  {
    this.bits = bits;
    editor.setBits( bits );
  }

  /**
   * Sets the base.
   * 
   * @param base the new base
   */
  public void setBase( int base )
  {
    this.base = base;
    editor.setBase( base );
    renderer.setBase( base );
  }
  
  /**
   * Gets the base.
   * 
   * @return the base
   */
  public int getBase(){ return base; }

  /** The editor. */
  private ByteEditor editor;
  
  /** The renderer. */
  private ByteRenderer renderer;
  
  /** The bits. */
  private int bits = 8;
  
  /** The base. */
  private int base = 10;
  
  /** The value. */
  private Object value = null;
}
