package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class CmdParameter.
 */
public abstract class CmdParameter
  extends Parameter
{
  
  /**
   * Instantiates a new cmd parameter.
   * 
   * @param name the name
   */
  public CmdParameter( String name )
  {
    super( name );
  }

  /**
   * Instantiates a new cmd parameter.
   * 
   * @param name the name
   * @param defaultValue the default value
   */
  public CmdParameter( String name, DefaultValue defaultValue )
  {
    super( name, defaultValue );
  }

  /**
   * Gets the value.
   * 
   * @param value the value
   * 
   * @return the value
   */
  public Object getValue( Object value )
  {
    if (( defaultValue != null ) &&
        ( defaultValue.getClass() == IndirectDefaultValue.class ) &&
        ( defaultValue.value() != null ) &&
        defaultValue.value().equals( value ))
      return null;
    else
      return value;
  }
  
  /**
   * Convert value.
   * 
   * @param value the value
   * 
   * @return the object
   */
  public Object convertValue( Object value )
  {
    if (( defaultValue != null ) && ( value == null ))
      return defaultValue.value();
    return value;
  }
  
  /**
   * Gets the editor.
   * 
   * @return the editor
   */
  public abstract TableCellEditor getEditor();
  
  /**
   * Gets the renderer.
   * 
   * @return the renderer
   */
  public abstract TableCellRenderer getRenderer();
  
  /**
   * Gets the value class.
   * 
   * @return the value class
   */
  public abstract Class<?> getValueClass();
  
  /** The optional. */
  private boolean optional = false;
  
  /**
   * Checks if is optional.
   * 
   * @return true, if is optional
   */
  public boolean isOptional(){ return optional; }
  
  /**
   * Sets the optional.
   * 
   * @param flag the new optional
   */
  public void setOptional( boolean flag ){ optional = flag; }
}
