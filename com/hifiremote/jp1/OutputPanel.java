package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.awt.datatransfer.*;

// TODO: Auto-generated Javadoc
/**
 * The Class OutputPanel.
 */
public class OutputPanel
  extends KMPanel implements ActionListener
{
  
  /**
   * Instantiates a new output panel.
   * 
   * @param deviceUpgrade the device upgrade
   */
  public OutputPanel( DeviceUpgrade deviceUpgrade )
  {
    super( "Output", deviceUpgrade );
    BoxLayout bl = new BoxLayout( this, BoxLayout.Y_AXIS );
    setLayout( bl );
    setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    includeNotes = new JCheckBox( "Include embedded notes in upgrades (requires IR v 5.01 or later)", true );
    includeNotes.addActionListener( this );
    add( includeNotes );

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

    protocolLabel = new JLabel( "Upgrade Protocol Code" );
    protocolLabel.setAlignmentY( 1f );
    box.add( protocolLabel );
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

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.KMPanel#update()
   */
  public void update()
  {
    boolean flag = includeNotes.isSelected();
    upgradeText.setText( deviceUpgrade.getUpgradeText( flag ));
    Protocol p = deviceUpgrade.getProtocol();
    Remote r = deviceUpgrade.getRemote();

    Hex code = deviceUpgrade.getCode();
    if ( code == null )
    {
      protocolLabel.setForeground( Color.BLACK );
      protocolLabel.setText( "Upgrade Protocol Code" );
      protocolText.setText( null );
    }
    else
    {
      protocolLabel.setForeground( Color.RED );
      protocolLabel.setText( "Upgrade Protocol Code *** REQUIRED ***" );
      Processor processor = r.getProcessor();
      StringBuilder buff = new StringBuilder( 300 );
      buff.append( "Upgrade protocol 0 = " );
      buff.append( p.getID( r ).toString());
      buff.append( " (" );
      buff.append( processor.getFullName());
      buff.append( ")" );
      if ( flag )
      {
        buff.append( ' ' );
        buff.append( p.getName());
        buff.append( " (RM " );
        buff.append( RemoteMaster.version );
        buff.append( ')' );
      }
      
      try 
      {
        BufferedReader rdr = new BufferedReader( new StringReader( code.toString( 16 )));
        String line = null;
        while (( line = rdr.readLine()) != null )
        {
          buff.append( "\n " );
          buff.append( line );
        }
      }
      catch ( IOException ioe )
      {
        ioe.printStackTrace( System.err );
      }
      buff.append( "\nEnd" );
      protocolText.setText( buff.toString());

      deviceUpgrade.checkSize();
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    JTextArea area = null;
    Object source = e.getSource();
    if ( source == includeNotes )
    {
      update();
      return;
    }
    if ( source == copyDeviceUpgrade )
      area = upgradeText;
    else if ( source == copyProtocolUpgrade )
      area = protocolText;
    else // assume copyItem
      area = popover;

    StringSelection data = new StringSelection( area.getText());
    clipboard.setContents( data, data );
  }

  /** The protocol label. */
  private JLabel protocolLabel = null;
  
  /** The upgrade text. */
  private JTextArea upgradeText = null;
  
  /** The protocol text. */
  private JTextArea protocolText = null;
  
  /** The popover. */
  private JTextArea popover = null;
  
  /** The popup. */
  private JPopupMenu popup = null;
  
  /** The copy item. */
  private JMenuItem copyItem = null;
  
  /** The copy device upgrade. */
  private JButton copyDeviceUpgrade = null;
  
  /** The copy protocol upgrade. */
  private JButton copyProtocolUpgrade = null;
  
  /** The clipboard. */
  private Clipboard clipboard = null;
  
  /** The include notes. */
  private JCheckBox includeNotes = null;
}
