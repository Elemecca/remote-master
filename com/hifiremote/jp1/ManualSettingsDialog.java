package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import info.clearthought.layout.*;

public class ManualSettingsDialog
  extends JDialog
  implements ActionListener
{
  public ManualSettingsDialog( JFrame owner, Protocol p )
  {
    super( owner, "Manual Settings", true );
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();

    this.protocol = p;

    double i = 5;        // space between rows and around border
    double c = 10;       // space between columns
    double f = TableLayout.FILL;
    double pr = TableLayout.PREFERRED;
    double size[][] =
    {
      { i, pr, c, pr, i },              // cols
      { i, pr, i, f, i, pr, i, f }         // rows
    };
    TableLayout tl = new TableLayout( size );
    JPanel mainPanel = new JPanel( tl );
    contentPane.add( mainPanel, BorderLayout.CENTER );
    
    JLabel label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 1" );

    pid = new JTextField();
    pid.setText( protocol.getID().toString());
    mainPanel.add( pid, "3, 1" );

    AbstractTableModel model = new AbstractTableModel()
    {
      public int getRowCount()
      {
        return protocol.getDeviceParameters().length;
      }

      public int getColumnCount()
      {
        return 5;
      }

      public String getColumnName( int col )
      {
        if ( col == 0 )
          return "Name";
        else if ( col == 1 )
          return "Type";
        else if ( col == 2 )
          return "Bits";
        else if ( col == 3 )
          return "Style";
        else if ( col == 4 )
          return "Comp";
        return null;
      }
      
      public Class getColumnClass( int col )
      {
        if ( col == 0 )
          return String.class;
        else if ( col == 1 )
          return String.class;
        else if ( col == 2 )
          return Integer.class;
        else if ( col == 3 )
          return String.class;
        else if ( col == 4 )
          return Boolean.class;
        return null;
      }

      public Object getValueAt( int row, int col )
      {
        DeviceParameter parm = protocol.getDeviceParameters()[ row ];
        Translator translator = ( Translator )protocol.getDeviceTranslators()[ row ];
        if ( col == 0 )
          return parm.getName();
        else if ( col == 1 )
          return parm.getDescription();
        else if ( col == 2 )
          return new Integer( translator.getBits());
        else if ( col == 3 )
        {
          if ( translator.getLSB())
            return "LSB";
          else
            return "MSB";
        }
        else
          return Boolean.valueOf( translator.getComp());
      }
    };
    JTable table = new JTable( model );
    JScrollPane scrollPane = new JScrollPane( table );
    Box box = Box.createVerticalBox();
    box.setBorder( BorderFactory.createTitledBorder( "Device Parameters" ));
    box.add( scrollPane );
    mainPanel.add( box, "1, 3, 3, 3" );
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    buttonPanel.add( new JButton( "Add" ));
    buttonPanel.add( new JButton( "Edit" ));
    buttonPanel.add( new JButton( "Delete" ));
    box.add( buttonPanel );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.height = 100;
    table.setPreferredScrollableViewportSize( d );

    label = new JLabel( "Raw Fixed Data:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 5" );
    rawHexData = new JTextField();
    mainPanel.add( rawHexData, "3, 5" );

    model = new AbstractTableModel()
    {
      public int getRowCount()
      {
        return protocol.getCommandParameters().length;
      }

      public int getColumnCount()
      {
        return 5;
      }

      public String getColumnName( int col )
      {
        if ( col == 0 )
          return "Name";
        else if ( col == 1 )
          return "Type";
        else if ( col == 2 )
          return "Bits";
        else if ( col == 3 )
          return "Style";
        else if ( col == 4 )
          return "Comp";
        return null;
      }
      
      public Class getColumnClass( int col )
      {
        if ( col == 0 )
          return String.class;
        else if ( col == 1 )
          return String.class;
        else if ( col == 2 )
          return Integer.class;
        else if ( col == 3 )
          return String.class;
        else if ( col == 4 )
          return Boolean.class;
        return null;
      }

      public Object getValueAt( int row, int col )
      {
        CmdParameter parm = protocol.getCommandParameters()[ row ];
        Translator translator = ( Translator )protocol.getCmdTranslators()[ row ];
        if ( col == 0 )
          return parm.getName();
        else if ( col == 1 )
          return parm.getDescription();
        else if ( col == 2 )
          return new Integer( translator.getBits());
        else if ( col == 3 )
        {
          if ( translator.getLSB())
            return "LSB";
          else
            return "MSB";
        }
        else
          return Boolean.valueOf( translator.getComp());
      }
    };
    table = new JTable( model );
    scrollPane = new JScrollPane( table );
    box = Box.createVerticalBox();
    box.setBorder( BorderFactory.createTitledBorder( "Command Parameters" ));
    box.add( scrollPane );
    mainPanel.add( box, "1, 7, 3, 7" );
    buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    buttonPanel.add( new JButton( "Add" ));
    buttonPanel.add( new JButton( "Edit" ));
    buttonPanel.add( new JButton( "Delete" ));
    box.add( buttonPanel );
    d = table.getPreferredScrollableViewportSize();
    d.height = 100;
    table.setPreferredScrollableViewportSize( d );

    buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));

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

  private Protocol protocol = null;

  private JTextField pid = null;

  // Device parameter stuff.
  private JTable table = null;
  private JTextField rawHexData = null; 

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
