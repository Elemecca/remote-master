package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class PortDialog
  extends JDialog
  implements ActionListener
{
  public PortDialog( JFrame owner, String[] portNames, String port )
  {
    super( owner, "Port Selection", true );
    createGui( owner, portNames, port );
  }

  public PortDialog( JDialog owner, String[] portNames, String port )
  {
    super( owner, "Port Selection", true );
    createGui( owner, portNames, port );
  }

  private void createGui( Component owner, String[] portNames, String port )
  {
    setLocationRelativeTo( owner );

    (( JPanel )getContentPane()).setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

    if (( port == null ) || port.equals( "" ))
      port = AUTODETECT;

    ButtonGroup group = new ButtonGroup();
    JRadioButton[] buttons = new JRadioButton[ portNames.length + 2 ];


    int i = 0;
    buttons[ i++ ] = new JRadioButton( AUTODETECT );
    buttons[ 0 ].setAlignmentX( Component.LEFT_ALIGNMENT );
    for( String name : portNames )
      buttons[ i++ ] = new JRadioButton( name );
    buttons[ i++ ] = new JRadioButton( OTHER );

    Box box = Box.createVerticalBox();
    box.add( new JLabel( "Select the desired port:" ));
    box.add( box.createVerticalStrut( 5 ));
    JPanel panel = new JPanel( new GridLayout( 0, 3, 5, 5 ));
    panel.setAlignmentX( Component.LEFT_ALIGNMENT );
    boolean foundMatch = false;
    for ( int j = 0; j < buttons.length; j++ )
    {
      JRadioButton button = buttons[ j ];
      group.add( button );
      button.addActionListener( this );
      String text = button.getText();
      if ( port.equals( text ))
      {
        button.setSelected( true );
        foundMatch = true;
      }
      if ( text.equals( OTHER ) && (( j % 3 ) == 2 ))
        panel.add( new JLabel());
      if ( j == 0 )
      {
        box.add( button );
        box.add( box.createVerticalStrut( 5 ));
        box.add( panel );
        box.add(  box.createVerticalStrut( 5 ));
      }
      else if ( j == ( buttons.length -1 ))
      {
        JPanel otherPanel = new JPanel( new FlowLayout( FlowLayout.LEADING, 0, 0 ));
        otherPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        box.add( otherPanel );
        otherPanel.add( button );
        otherPanel.add( Box.createHorizontalStrut( 5 ));
        otherPanel.add( other );
      }
      else
        panel.add( button );
    }
    if ( !foundMatch )
    {
      other.setText( port );
      buttons[ buttons.length - 1 ].setSelected( true );
    }
    
    add( box, BorderLayout.NORTH );

    panel = new JPanel( new FlowLayout( FlowLayout.TRAILING ));

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    panel.add( ok );
    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    panel.add( cancel );

    add( panel, BorderLayout.SOUTH );

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
  }

  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == cancel )
    {
      userAction = JOptionPane.CANCEL_OPTION;
      setVisible( false );
      dispose();
    }
    else if ( source == ok )
    {
      userAction = JOptionPane.OK_OPTION;
      setVisible( false );
      dispose();
    }
    else
    {
      JRadioButton button = ( JRadioButton )source;
      port = button.getText();
      other.setEnabled( OTHER.equals( port ));
    }
  }

  public String getPort()
  {
    if ( OTHER.equals( port ))
      port = other.getText();
    return port;
  }

  public int getUserAction()
  {
    return userAction;
  }

  private String port = null;
  private JTextField other = new JTextField( 15 );
  private JButton ok = null;
  private JButton cancel = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
  
  public final static String AUTODETECT = "Auto-detect";
  public final static String OTHER = "Other:";
}
