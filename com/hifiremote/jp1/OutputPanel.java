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
    upgradeText.setBackground( label.getBackground());
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
    protocolText.setBackground( label.getBackground());

    scroll = new JScrollPane( protocolText );
    add( scroll );
  }

  private int adjust( int val )
  {
    System.err.println( "adjust( " + Integer.toHexString( val ) + " )" );
    int temp1 = val - 0x2C;
    int temp2 = val - 0x46;
    if ((( 0 <= temp1 ) && ( temp1 <= 0x0E ) && ( temp1 % 7 == 0 )) ||
        (( 0 <= temp2 ) && ( temp2 <= 0x0E ) && ( temp2 % 3 == 0 )))
    {
      val -= 0x13;
    }
    System.err.println( "Returning " + Integer.toHexString( val ));
    return val;
  }

  public void update()
  {
    upgradeText.setText( deviceUpgrade.getUpgradeText());
    Protocol p = deviceUpgrade.getProtocol();
    Remote r = deviceUpgrade.getRemote();
    String processor = r.getProcessor();
    Hex code = p.getCode( processor );
    if ( code != null )
    {
      System.err.println( "Checking if translation needed for proceccor " + processor + " and RAMAddr " + Integer.toHexString( r.getRAMAddress() ));
      byte[] data = ( byte[] )code.getData().clone();
      if ( processor.equals( "S3C80" ) && ( r.getRAMAddress() == 0x8000 ))
      {
        int offset = 3;
        if (( data[ 3 ] & 0xFF ) == 0x8B )
        {
          offset = ( data[ 4 ] & 0xFF ) + 5;
          System.err.println( "Code doesn't start at 3, it starts at " + offset );
        }
        for ( int i = offset; i < data.length; i++ )
        {
          int first = data[ i ] & 0xFF;
          System.err.println( "Checking byte at " + i + ": " + Integer.toHexString( first ));
          if ( first == 0xF6 )
          {
            int second = data[ ++i ] & 0xFF;
            System.err.println( "Got F6, next byte is " + Integer.toHexString( second ));
            if ( second == 0xFF )
            {
              System.err.println( "Got 0xFF, changing to 0x80" );
              data[ i ] = ( byte )0x80;
            }
            else if ( second == 0x01 )
            {
              int third = data[ ++i ] & 0xFF;
              System.out.println( "Got 0x01, next byte is " + Integer.toHexString( third ));
              data[ i ] = ( byte )adjust( third );
            }
          }
          else if ( first == 0x8D )
          {
            int second = data[ ++i ] & 0xFF;
            if ( second == 0x01 )
            {
              int third = data[ ++i ] & 0xFF;
              data[ i ] = ( byte )adjust( third );
            }
          }
        }
      }
      StringBuffer buff = new StringBuffer( 300 );
      buff.append( "Upgrade protocol 0 = " );
      buff.append( p.getID().toString());
      buff.append( " (" );
      buff.append( processor );
      buff.append( ")\n " );
      buff.append( Hex.toString( data, 16 ));
      buff.append( "\nEnd" );
      protocolText.setText( buff.toString());
    }
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
