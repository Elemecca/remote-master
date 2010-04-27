package com.hifiremote.jp1;

import javax.swing.JPanel;

// TODO: Auto-generated Javadoc
/**
 * The Class KMPanel.
 */
public class KMPanel
  extends JPanel
{
  
  /**
   * Instantiates a new kM panel.
   * 
   * @param name the name
   * @param devUpgrade the dev upgrade
   */
  public KMPanel( String name, DeviceUpgrade devUpgrade )
  {
    setName( name );
    this.deviceUpgrade = devUpgrade;
  }

  /**
   * Sets the device upgrade.
   * 
   * @param devUpgrade the new device upgrade
   */
  public void setDeviceUpgrade( DeviceUpgrade devUpgrade )
  {
    deviceUpgrade = devUpgrade;
  }

  /** The device upgrade. */
  protected DeviceUpgrade deviceUpgrade = null;

  /**
   * Commit.
   */
  public void commit(){}
  
  /**
   * Update.
   */
  public void update(){}
  
  /**
   * Release
   */
  public void release(){}
}

