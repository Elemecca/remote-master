package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// TODO: Auto-generated Javadoc
/**
 * The Class CustomNameDialog.
 */
public class CustomNameDialog
  extends JDialog
  implements ActionListener
{
  
  /**
   * Instantiates a new custom name dialog.
   * 
   * @param owner the owner
   * @param customNames the custom names
   */
  public CustomNameDialog( JFrame owner, String[] customNames )
  {
    super( owner, "Custom Function Names", true );
    createGui( owner, customNames );
  }
    
  /**
   * Instantiates a new custom name dialog.
   * 
   * @param owner the owner
   * @param customNames the custom names
   */
  public CustomNameDialog( JDialog owner, String[] customNames )
  {
    super( owner, "Custom Function Names", true );
    createGui( owner, customNames );
  }
    
  /**
   * Creates the gui.
   * 
   * @param owner the owner
   * @param customNames the custom names
   */
  private void createGui( Component owner, String[] customNames )
  {
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
    else if ( source == getButtonNames )
    {
       Button[] buttons = KeyMapMaster.getRemote().getUpgradeButtons();

       for ( int i = 0; i < buttons.length; i++ )
       {
         if ( i > 0 )
           textArea.append( "\n" );
         textArea.append( buttons[ i ].getName());
       }
    }
  }

  /**
   * Gets the custom names.
   * 
   * @return the custom names
   */
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

  /**
   * Gets the user action.
   * 
   * @return the user action
   */
  public int getUserAction()
  {
    return userAction;
  }

  /** The text area. */
  private JTextArea textArea = null;
  
  /** The get button names. */
  private JButton getButtonNames = null;
  
  /** The ok. */
  private JButton ok = null;
  
  /** The cancel. */
  private JButton cancel = null;
  
  /** The user action. */
  private int userAction = JOptionPane.CANCEL_OPTION;
}
