package com.hifiremote.jp1;

import java.awt.event.ItemListener;
import java.util.EventListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

// TODO: Auto-generated Javadoc
/**
 * The Class FlagDeviceParm.
 */
public class FlagDeviceParm
  extends DeviceParameter
{
  
  /**
   * Instantiates a new flag device parm.
   * 
   * @param name the name
   * @param defaultValue the default value
   */
  public FlagDeviceParm( String name, DefaultValue defaultValue )
  {
    super( "", defaultValue );
    checkBox = new JCheckBox( name );
    setValue( defaultValue.value());
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getValue()
   */
  public Object getValue()
  {
    if ( checkBox.isSelected())
      return new Integer(1);
    else
      return new Integer(0);
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#setValue(java.lang.Object)
   */
  public void setValue( Object value )
  {
    boolean flag = false;
    if ( value != null )
    {
      if ( value.getClass() == Integer.class )
        flag = ((( Integer )value ).intValue() != 0 );
    }
    checkBox.setSelected( flag );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DeviceParameter#getComponent()
   */
  public JComponent getComponent(){ return checkBox; }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DeviceParameter#addListener(java.util.EventListener)
   */
  public void addListener( EventListener l )
  {
    checkBox.addItemListener(( ItemListener )l );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DeviceParameter#removeListener(java.util.EventListener)
   */
  public void removeListener( EventListener l )
  {
    checkBox.removeItemListener(( ItemListener )l );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getDescription()
   */
  public String getDescription(){ return "Flag"; }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getName()
   */
  public String getName(){ return checkBox.getText();}

  /** The check box. */
  private JCheckBox checkBox = null;
}
