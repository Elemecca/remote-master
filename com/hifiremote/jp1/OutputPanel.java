package com.hifiremote.jp1;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class OutputPanel
  extends KMPanel implements ActionListener
{
  public OutputPanel( DeviceUpgrade deviceUpgrade )
  {
    super( deviceUpgrade );
    BoxLayout bl = new BoxLayout( this, BoxLayout.Y_AXIS );
    setLayout( bl );

    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    upgradeText = new JTextArea( 10, 40 );
    upgradeText.setBorder( BorderFactory.createTitledBorder( "Device Upgrade Code" ));
    upgradeText.setEditable( false );
    upgradeText.setDragEnabled( true );

    add( new JScrollPane( upgradeText ));

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

    protocolText = new JTextArea( 10, 40 );
    protocolText.setBorder( BorderFactory.createTitledBorder( "Upgrade Protocol Code" ));
    protocolText.setEditable( false );
    protocolText.setDragEnabled( true );
    add( new JScrollPane( protocolText ) );
    protocolText.addMouseListener( mh );
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
    StringSelection data = new StringSelection( popover.getText());
    clipboard.setContents( data, data );
  }

  private JTextArea upgradeText = null;
  private JTextArea protocolText = null;
  private JTextArea popover = null;
  private JPopupMenu popup = null;
  private JMenuItem copyItem = null;
  private Clipboard clipboard = null;

}
