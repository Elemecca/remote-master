package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class SetupCodeRenderer extends DefaultTableCellRenderer
{
  public SetupCodeRenderer( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
  }
  
  public Component getTableCellRendererComponent( JTable table, Object value, 
      boolean isSelected, boolean hasFocus,
      int row, int col )
  {
    Component c = super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
    DeviceButtonTableModel dbTableModel = ( DeviceButtonTableModel )table.getModel();
    deviceType = ( DeviceType )dbTableModel.getValueAt( row, 2 );
    deviceButton = dbTableModel.getRow( row );
    SetupCode setupCode = ( SetupCode )value;
    if ( deviceType != null && setupCode != null )
    {
      c.setForeground( getTextColor( setupCode.getValue(), isSelected ) );
    }
    return c;
  }
  
  private boolean isValidUpgrade( int setupCodeValue )
  {
    for ( DeviceUpgrade devUpgrade : remoteConfig.getDeviceUpgrades() )
    {
      if ( deviceType.getNumber() == devUpgrade.getDeviceType().getNumber()
          && setupCodeValue == devUpgrade.getSetupCode()
          && ( devUpgrade.getButtonIndependent() 
              || deviceButton.getButtonIndex() == devUpgrade.getButtonRestriction().getButtonIndex() ) )
      {
        return true;
      }       
    }
    return false;    
  }
  
  public boolean isValid( int setupCodeValue )
  {
    Remote remote = remoteConfig.getRemote();
    if ( remote.getSetupValidation() == Remote.SetupValidation.OFF )
    {
      return true;
    }
    return remote.hasSetupCode( deviceType, setupCodeValue ) || isValidUpgrade( setupCodeValue );
  }
  
  public Color getTextColor( int setupCodeValue, boolean isSelected )
  {
    if ( isValid( setupCodeValue ) )
    {
      return isSelected ? Color.WHITE : Color.BLACK;
    }
    else
    {
      return isSelected ? Color.YELLOW : Color.RED;
    }    
  }
  
  public void setDeviceButton( DeviceButton deviceButton )
  {
    this.deviceButton = deviceButton;
  }

  public void setDeviceType( DeviceType deviceType )
  {
    this.deviceType = deviceType;
  }

  private RemoteConfiguration remoteConfig = null;
  private DeviceButton deviceButton = null;
  private DeviceType deviceType = null;
  
}
