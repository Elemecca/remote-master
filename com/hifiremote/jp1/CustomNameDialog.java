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
  public CustomNameDialog( JFrame owner, String[] customNames )
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

    Container contentPane = getContentPane();

    JLabel instructions = new JLabel( "Enter the desired default functions names, one on each line." );
    contentPane.add( instructions, BorderLayout.NORTH );

    contentPane.add( new JScrollPane( textArea ), BorderLayout.CENTER );

    JPanel buttonPanel = new JPanel();
    FlowLayout fl = ( FlowLayout )buttonPanel.getLayout();
    fl.setAlignment( FlowLayout.RIGHT );

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    buttonPanel.add( ok );

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

  private JTextArea textArea = null;
  private JButton ok = null;
  private JButton cancel = null;
  private JPopupMenu popup = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
}
