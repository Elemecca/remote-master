package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class CustomNameDialog
  extends JDialog
  implements ActionListener
{
  public CustomNameDialog( JFrame owner, String[] customNames, DeviceUpgrade upgrade )
  {
    super( owner, "Custom Function Names", true );
    setLocationRelativeTo( owner );

    textArea = new JTextArea( 20, 20 );
    new TextPopupMenu( textArea );

    if ( customNames != null )
    {
      for ( int i = 0; i < customNames.length; i++ )
      {
        if ( i != 0 )
          textArea.append( "\n" );
               
        textArea.append( customNames[ i ]);
      }
    }

    this.upgrade = upgrade;

    Container contentPane = getContentPane();

    JLabel instructions = new JLabel( "Enter the desired default functions names, one on each line." );
    instructions.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    contentPane.add( instructions, BorderLayout.NORTH );

    JScrollPane scroll = new JScrollPane( textArea );
    scroll.setBorder( 
      BorderFactory.createCompoundBorder( 
        BorderFactory.createEmptyBorder( 0, 5, 0, 5 ),
        scroll.getBorder()));
    contentPane.add( scroll, BorderLayout.CENTER );

    Box buttonPanel = Box.createHorizontalBox();
    buttonPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

    getButtonNames = new JButton( "Get Button Names" );
    getButtonNames.setToolTipText( "Add the names of the buttons on the current remote." );
    getButtonNames.addActionListener( this );
    buttonPanel.add( getButtonNames );

    buttonPanel.add( Box.createHorizontalGlue());

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    buttonPanel.add( ok );

    buttonPanel.add( Box.createHorizontalStrut( 5 ));

    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    buttonPanel.add( cancel );

    contentPane.add( buttonPanel, BorderLayout.SOUTH );

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
    else if ( source == getButtonNames )
    {
       Button[] buttons = upgrade.getRemote().getUpgradeButtons();
       
       for ( int i = 0; i < buttons.length; i++ )
       {
         if ( i > 0 )
           textArea.append( "\n" );
         textArea.append( buttons[ i ].getName());
       }
    }
  }

  public String[] getCustomNames()
  {
    StringTokenizer st = new StringTokenizer( textArea.getText().trim(), "\r\n" );
    int count = st.countTokens();
    if ( count == 0 )
      return null;
    String[] customNames = new String[ count ];
    int i = 0;
    while ( st.hasMoreTokens())
      customNames[ i++ ] = st.nextToken();

    return customNames;
  }

  public int getUserAction()
  {
    return userAction;
  }
 
  private DeviceUpgrade upgrade = null;
  private JTextArea textArea = null;
  private JButton getButtonNames = null;
  private JButton ok = null;
  private JButton cancel = null;
  private JPopupMenu popup = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
}
