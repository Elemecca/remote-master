package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import info.clearthought.layout.*;

public class ManualSettingsDialog
  extends JDialog
  implements ActionListener
{
  public ManualSettingsDialog( JFrame owner, ManualProtocol protocol )
  {
    super( owner, "Manual Settings", true );
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();

    Border border = BorderFactory.createTitledBorder( "Protocol Parameters" );
    JPanel devPanel = new JPanel();
    devPanel.setBorder( border );

    Insets insets = border.getBorderInsets( devPanel );
    double bt = insets.top;
    double bl = insets.left + 10;
    double br = insets.right;
    double bb = insets.bottom;
    double b = 10;       // space around border
    double i = 5;        // space between rows
    double v = 20;       // space between groupings
    double c = 10;       // space between columns
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
      { i, bl, p, b, p, p, br, i },                     // cols
      { i, p, i, bt, p, i, p, i, p, i, p, i, p, bb }         // rows
    };
    TableLayout tl = new TableLayout( size );
    JPanel mainPanel = new JPanel( tl );
    contentPane.add( mainPanel, BorderLayout.CENTER );
    
    JLabel label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 1" );

    pid = new JTextField();
    mainPanel.add( pid, "4, 1, 5, 1" );

    label = new JLabel( "Number of parameters:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 4" );

    devParmCount = new JSpinner( new SpinnerNumberModel( 0, 0, 3, 1 ));
    mainPanel.add( devParmCount, "4, 4" );

    label = new JLabel( "Bit order:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 6" );
    ButtonGroup group = new ButtonGroup();
    devParmLSB = new JRadioButton( "LSB" );
    group.add( devParmLSB );
    devParmMSB = new JRadioButton( "MSB" );
    group.add( devParmMSB );
    mainPanel.add( devParmLSB, "4, 6" );
    mainPanel.add( devParmMSB, "5, 6" );

    label = new JLabel( "Style:", SwingConstants.RIGHT );  
    mainPanel.add( label, "2, 8" );
    devParmComp = new JCheckBox( "Comp" );
    mainPanel.add( devParmComp, "4, 8, 5, 8" );
    
    label = new JLabel( "Number of Bits:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 10" );
    devParmBits = new JSpinner( new SpinnerNumberModel( 8, 1, 8, 1 ));
    mainPanel.add( devParmBits, "4, 10" );

    label = new JLabel( "Raw Fixed Data:", SwingConstants.RIGHT );
    mainPanel.add( label, "2, 12" );
    rawHexData = new JTextField();
    mainPanel.add( rawHexData, "4, 12, 5, 12" );

    mainPanel.add( devPanel, "1, 3, 6, 13" );
        
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

  public int getUserAction()
  {
    return userAction;
  }

  private JTextField pid = null;

  // Device parameter stuff.
  private JSpinner devParmCount = null;
  private JRadioButton devParmLSB = null;
  private JRadioButton devParmMSB = null;
  private JCheckBox devParmComp = null;
  private JTextField rawHexData = null; 
  private JSpinner devParmBits = null;

  // CommandParameter stuff
  private JRadioButton cmdParmLSB = null;
  private JRadioButton cmdParmMSB = null;
  private JCheckBox cmdParmComp = null;
  private JSpinner cmdParmBits = null;
  private JComboBox cmdSecondByte = null;

  private JTextArea protocolCode = null;

  private JButton ok = null;
  private JButton cancel = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
}
