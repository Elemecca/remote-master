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
  public ManualSettingsDialog( JFrame owner, Protocol protocol )
  {
    super( owner, "Manual Settings", true );
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();
    
    {
      System.err.println( "Copying device parameters" );
      DeviceParameter[] parms = protocol.getDeviceParameters();
      if ( parms != null )
      {
        Translate[] xlators = protocol.getDeviceTranslators();
        for ( int i = 0; i < parms.length; i++ )
        {
          deviceParms.add( parms[ i ]);
          deviceTranslators.add( xlators[ i ]);
        }
      }
    }

    double b = 5;        // space between rows and around border
    double c = 10;       // space between columns
    double f = TableLayout.FILL;
    double pr = TableLayout.PREFERRED;
    double size[][] =
    {
      { b, pr, c, pr, b },              // cols
      { b, pr, b, f, b, pr, b, f }         // rows
    };
    TableLayout tl = new TableLayout( size );
    JPanel mainPanel = new JPanel( tl );
    contentPane.add( mainPanel, BorderLayout.CENTER );
    
    JLabel label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 1" );

    pid = new JTextField();
    Hex id = protocol.getID();
    if ( id != null )
      pid.setText( id.toString());
    mainPanel.add( pid, "3, 1" );

    AbstractTableModel model = new AbstractTableModel()
    {
      public int getRowCount()
      {
        return deviceParms.size(); 
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
        DeviceParameter parm = ( DeviceParameter )deviceParms.get( row );
        Translator translator = ( Translator )deviceTranslators.get( row );
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
    addDevice = new JButton( "Add" );
    addDevice.addActionListener( this );
    buttonPanel.add( addDevice );
    editDevice = new JButton( "Edit" );
    editDevice.addActionListener( this );
    buttonPanel.add( editDevice );
    deleteDevice = new JButton( "Delete" );
    deleteDevice.addActionListener( this );
    buttonPanel.add( deleteDevice );
    box.add( buttonPanel );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.height = 100;
    table.setPreferredScrollableViewportSize( d );

    label = new JLabel( "Raw Fixed Data:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 5" );
    rawHexData = new JTextField();
    rawHexData.setText( protocol.getFixedData( new Value[ 0 ]).toString());
    mainPanel.add( rawHexData, "3, 5" );

    {
      System.err.println( "Copying comand parameters" );
      CmdParameter[] parms = protocol.getCommandParameters();
      if ( parms != null )
      {
        Translate[] xlators = protocol.getCmdTranslators();
        for ( int i = 0; i < parms.length; i++ )
        {
          cmdParms.add( parms[ i ]);
          cmdTranslators.add( xlators[ i ]);
        }
      }
    }

    model = new AbstractTableModel()
    {
      public int getRowCount()
      {
        return cmdParms.size();
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
        CmdParameter parm = ( CmdParameter )cmdParms.get( row );
        Translator translator = ( Translator )cmdTranslators.get( row );
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
    if ( source == addDevice )
    {
      String name = 
        ( String )JOptionPane.showInputDialog( this, "Please provide a name for the device parameter." );
      if ( name == null )
        return;
      Object[] types = { "Numeric entry", "Drop-down list", "Check-box" };
      String type = ( String )JOptionPane.showInputDialog( this, 
                                                        "How will the parameter \"" + name + "\" be presented to the user?",
                                                        "Device Parameter Type", 
                                                        JOptionPane.QUESTION_MESSAGE,
                                                        null,
                                                        types,
                                                        types[ 0 ]);
      if ( type == null )
        return;
      int bits = 0;
      if ( type.equals( types[ 0 ]))
      {
        Object[] choices = { "8", "7", "6", "5", "4", "3", "2", "1" };
        String temp = ( String )JOptionPane.showInputDialog( this, 
                                                             "How many bits are required to store the \ndevice parameter \"" + name + "\"?",
                                                             "Numeric Device Parameter Bit Length",
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             choices,
                                                             choices[ 0 ]);
        if ( temp == null )
          return;
        bits = Integer.parseInt( temp );
      }
      else if ( type.equals( types[ 1 ]))
      {
        JTextArea textArea = new JTextArea( 8, 20 );
        new TextPopupMenu( textArea );
        Box box = Box.createVerticalBox();
        box.add( new JLabel( "Provide the choices for the paramter \"" + name + ",\" one on each line." ));
        box.add( new JScrollPane( textArea ));
        int temp = JOptionPane.showConfirmDialog( this, box, "Drop-down list choices", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
        if ( temp == JOptionPane.CANCEL_OPTION )
          return;
      }
      else
        bits = 1;
                                                        
                                                                  
    }
    else if ( source == cancel )
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

  private Vector deviceParms = new Vector();
  private Vector deviceTranslators = new Vector();
  private Vector cmdParms = new Vector(); 
  private Vector cmdTranslators = new Vector();

  private JTextField pid = null;

  // Device parameter stuff.
  private JTable table = null;
  private JTextField rawHexData = null; 

  // CommandParameter stuff
  private JTextArea protocolCode = null;

  private JButton addDevice = null;
  private JButton editDevice = null;
  private JButton deleteDevice = null;

  private JButton addCommand = null;
  private JButton editCommand = null;
  private JButton deleteCommand = null;

  private JButton ok = null;
  private JButton cancel = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
}
