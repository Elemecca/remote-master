package com.hifiremote.jp1;

import javax.swing.JPanel;

public class KMPanel
  extends JPanel
{
  public KMPanel( DeviceUpgrade devUpgrade )
  {
    this.deviceUpgrade = devUpgrade;
  }

  protected DeviceUpgrade deviceUpgrade = null;

  public void commit(){}
  public void update(){}
}

