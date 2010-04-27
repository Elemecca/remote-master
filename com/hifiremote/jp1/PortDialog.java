package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

// TODO: Auto-generated Javadoc
/**
 * The Class PortDialog.
 */
public class PortDialog
  extends JDialog
  implements ActionListener
{
  
  /**
   * Instantiates a new port dialog.
   * 
   * @param owner the owner
   * @param portNames the port names
   * @param port the port
   */
  public PortDialog( JFrame owner, String[] portNames, String port )
  {
    super( owner, "Port Selection", true );
    createGui( owner, portNames, port );
  }

  /**
   * Instantiates a new port dialog.
   * 
   * @param owner the owner
   * @param portNames the port names
   * @param port the port
   */
  public PortDialog( JDialog owner, String[] portNames, String port )
  {
    super( owner, "Port Selection", true );
    createGui( owner, portNames, port );
  }

  /**
   * Creates the gui.
   * 
   * @param owner the owner
   * @param portNames the port names
   * @param port the port
   */
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
    box.add( Box.createVerticalStrut( 5 ));
    int numColumns = 3;
    if ( portNames.length < 3 )
      numColumns = portNames.length;
    if ( numColumns == 0 )
      numColumns = 1;
    JPanel panel = new JPanel( new GridLayout( 0, numColumns, 5, 5 ));
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
//      if ( text.equals( OTHER ) && (( j % 3 ) == 2 ))
//        panel.add( new JLabel());
      if ( j == 0 )
      {
        box.add( button );
        box.add( Box.createVerticalStrut( 5 ));
        box.add( panel );
        box.add(  Box.createVerticalStrut( 5 ));
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

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
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

  /**
   * Gets the port.
   * 
   * @return the port
   */
  public String getPort()
  {
    if ( OTHER.equals( port ))
      port = other.getText();
    return port;
  }

  /**
   * Gets the user action.
   * 
   * @return the user action
   */
  public int getUserAction()
  {
    return userAction;
  }

  /** The port. */
  private String port = null;
  
  /** The other. */
  private JTextField other = new JTextField( 15 );
  
  /** The ok. */
  private JButton ok = null;
  
  /** The cancel. */
  private JButton cancel = null;
  
  /** The user action. */
  private int userAction = JOptionPane.CANCEL_OPTION;
  
  /** The Constant AUTODETECT. */
  public final static String AUTODETECT = "Auto-detect";
  
  /** The Constant OTHER. */
  public final static String OTHER = "Other:";
}
