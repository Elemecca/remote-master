package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;

public class OutputPanel
  extends KMPanel implements ActionListener
{
  public OutputPanel( DeviceUpgrade deviceUpgrade )
  {
    super( deviceUpgrade );
    BoxLayout bl = new BoxLayout( this, BoxLayout.Y_AXIS );
    setLayout( bl );
    setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    Box box = Box.createHorizontalBox();
    box.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ));
    add( box );
    
    JLabel label = new JLabel( "Device Upgrade Code" );
    label.setAlignmentY( 1f );
    box.add( label );

    box.add( box.createHorizontalGlue());

    copyDeviceUpgrade = new JButton( "Copy" );
    copyDeviceUpgrade.setAlignmentY( 1f );
    copyDeviceUpgrade.addActionListener( this );
    box.add( copyDeviceUpgrade );
    
    upgradeText = new JTextArea( 10, 40 );
    upgradeText.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    upgradeText.setEditable( false );
    upgradeText.setDragEnabled( true );
    JScrollPane scroll = new JScrollPane( upgradeText );
    add( scroll );

    popup = new JPopupMenu();
    copyItem = new JMenuItem( "Copy" );
    copyItem.addActionListener( this );
    popup.add( copyItem );

    MouseAdapter mh = new MouseAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        showPopup( e );
      }

      public void mouseReleased( MouseEvent e )
      {
        showPopup( e );
      }

      private void showPopup( MouseEvent e )
      {
        if ( e.isPopupTrigger() )
        {
          popover = ( JTextArea )e.getSource();
          popup.show( popover, e.getX(), e.getY());
        }
      }
    };
    upgradeText.addMouseListener( mh );

    add( Box.createVerticalStrut( 20 ));

    box = Box.createHorizontalBox();
    add( box );
    box.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ));

    label = new JLabel( "Upgrade Protocol Code" );
    label.setAlignmentY( 1f );
    box.add( label );
    box.add( box.createHorizontalGlue());

    copyProtocolUpgrade = new JButton( "Copy" );
    copyProtocolUpgrade.setAlignmentY( 1f );
    copyProtocolUpgrade.addActionListener( this );
    box.add( copyProtocolUpgrade );
    
    protocolText = new JTextArea( 10, 40 );
    protocolText.setEditable( false );
    protocolText.setDragEnabled( true );
    protocolText.addMouseListener( mh );
    protocolText.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

    scroll = new JScrollPane( protocolText );
    add( scroll );
  }

  public void update()
  {
    upgradeText.setText( deviceUpgrade.getUpgradeText());
    Protocol p = deviceUpgrade.getProtocol();
    Remote r = deviceUpgrade.getRemote();
    protocolText.setText( p.getCodeText( r.getProcessor()));
  }

  public void actionPerformed( ActionEvent e )
  {
    JTextArea area = null;
    Object source = e.getSource();
    if ( source == copyDeviceUpgrade )
      area = upgradeText;
    else if ( source == copyProtocolUpgrade )
      area = protocolText;
    else // assume copyItem
      area = popover;

    StringSelection data = new StringSelection( area.getText());
    clipboard.setContents( data, data );
  }

  private JTextArea upgradeText = null;
  private JTextArea protocolText = null;
  private JTextArea popover = null;
  private JPopupMenu popup = null;
  private JMenuItem copyItem = null;
  private JButton copyDeviceUpgrade = null;
  private JButton copyProtocolUpgrade = null;
  private Clipboard clipboard = null;

}
