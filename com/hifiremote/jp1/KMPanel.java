package com.hifiremote.jp1;

import javax.swing.JPanel;

public class KMPanel
  extends JPanel
{
  public KMPanel( String name, DeviceUpgrade devUpgrade )
  {
    setName( name );
    this.deviceUpgrade = devUpgrade;
  }

  public void setDeviceUpgrade( DeviceUpgrade devUpgrade )
  {
    deviceUpgrade = devUpgrade;
  }

  protected DeviceUpgrade deviceUpgrade = null;

  public void commit(){}
  public void update(){}
}

