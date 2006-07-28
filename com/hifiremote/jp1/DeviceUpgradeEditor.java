package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DeviceUpgradeEditor
  extends JDialog
  implements ActionListener
{
  public DeviceUpgradeEditor( JFrame owner, DeviceUpgrade deviceUpgrade, Remote[] remotes )
  {
    super( owner, "Device Upgade Editor", true );
    createGUI( owner, deviceUpgrade, remotes );
  }
  
  public DeviceUpgradeEditor( JDialog owner, DeviceUpgrade deviceUpgrade, Remote[] remotes )
  {
    super( owner, "Device Upgrade Editor", true );
    createGUI( owner, deviceUpgrade, remotes );
  }
  
  private void createGUI( Window owner, DeviceUpgrade deviceUpgrade, Remote[] remotes )
  {
    setLocationRelativeTo( owner );
    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
    addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent event )
      {
        cancelButton.doClick();
      }
    });
    editorPanel = new DeviceEditorPanel( deviceUpgrade, remotes );
    add( editorPanel, BorderLayout.CENTER );
    
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    add( buttonPanel, BorderLayout.SOUTH );
    
    buttonPanel.add( okButton );
    buttonPanel.add( cancelButton );
    
    okButton.addActionListener( this );
    cancelButton.addActionListener( this );
    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
    setVisible( true );
  }
  
  public DeviceUpgrade getDeviceUpgrade()
  {
    if ( cancelled )
      return null;
      
    return editorPanel.getDeviceUpgrade();
  }
  
  public void actionPerformed( ActionEvent e )
  {
    if ( e.getSource() == cancelButton )
      cancelled = true;
    setVisible( false );
  }
    
  private boolean cancelled = false;
  private DeviceEditorPanel editorPanel = null;
  private JButton okButton = new JButton( "OK" );
  private JButton cancelButton = new JButton( "Cancel" );
}
