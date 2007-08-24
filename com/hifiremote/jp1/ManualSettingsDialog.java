package com.hifiremote.jp1;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import info.clearthought.layout.*;

public class ManualSettingsDialog
  extends JDialog
  implements ActionListener, PropertyChangeListener, DocumentListener
{
  public ManualSettingsDialog( JDialog owner, ManualProtocol protocol )
  {
    super( owner, "Manual Settings", true );
    createGui( owner, protocol );
  }
  public ManualSettingsDialog( JFrame owner, ManualProtocol protocol )
  {
    super( owner, "Manual Settings", true );
    createGui( owner, protocol );
  }

  private void createGui( Component owner, ManualProtocol protocol )
  {
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();

    this.protocol = protocol;
    System.err.println( "protocol=" + protocol );

    double b = 5;        // space between rows and around border
    double c = 10;       // space between columns
    double f = TableLayout.FILL;
    double pr = TableLayout.PREFERRED;
    double size[][] =
    {
      { b, pr, c, pr, b },                        // cols
      { b, pr, b, pr, b, pr, b, pr, b, pr, b, pr, b }         // rows
    };
    TableLayout tl = new TableLayout( size );
    JPanel mainPanel = new JPanel( tl );
    contentPane.add( mainPanel, BorderLayout.CENTER );

    JLabel label = new JLabel( "Name:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 1" );
    name = new JTextField( protocol.getName());
    name.setEditable( false );
    name.getDocument().addDocumentListener( this );
    mainPanel.add( name, "3, 1" );

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 3" );

    pid = new JFormattedTextField( new HexFormat( 2, 2 ));
    new TextPopupMenu( pid );
    pid.addPropertyChangeListener( "value", this );
    mainPanel.add( pid, "3, 3" );

    // Protocol Code Table
    JPanel tablePanel = new JPanel( new BorderLayout());
    mainPanel.add( tablePanel, "1, 5, 3, 5" );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Protocol code" ));
    codeModel = new CodeTableModel();
    codeTable = new JTableX( codeModel );
    tablePanel.add( new JScrollPane( codeTable ), BorderLayout.CENTER );
    DefaultTableCellRenderer r = ( DefaultTableCellRenderer )codeTable.getDefaultRenderer( String.class );
    r.setHorizontalAlignment( SwingConstants.CENTER );
    codeTable.setDefaultEditor( Hex.class, new HexCodeEditor());

    JLabel l = ( JLabel )
      codeTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent( codeTable, colNames[ 0 ], false, false, 0, 0 );

    TableColumnModel columnModel = codeTable.getColumnModel();
    TableColumn column = columnModel.getColumn( 0 );
    int width = l.getPreferredSize().width;

    procs = ProcessorManager.getProcessors();
    int count = 0;
    for ( int i = 0; i < procs.length; i++ )
    {
      Processor proc = procs[ i ];
      if ( proc.getEquivalentName().equals( proc.getFullName()))
        ++count;
    }
    Processor[] uProcs = new Processor[ count ];
    count = 0;
    for ( int i = 0; i < procs.length; i++ )
    {
      Processor proc = procs[ i ];
      if ( proc.getEquivalentName().equals( proc.getFullName()))
        uProcs[ count++ ] = proc;
    }
    procs = uProcs;
    for ( int i = 0; i < procs.length; i++ )
    {
      l.setText( procs[ i ].getFullName());
      width =  Math.max( width, l.getPreferredSize().width );
    }
    for ( int i = 0; i < procs.length; i++ )
    {
      column.setMinWidth( width );
      column.setMaxWidth( width );
      column.setPreferredWidth( width );
    }
    codeTable.doLayout();
    codeTable.setPreferredScrollableViewportSize( codeTable.getPreferredSize() );

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    importButton = new JButton( "Import Protocol Upgrade" );
    importButton.addActionListener( this );
    importButton.setToolTipText( "Import Protocol Upgrades(s) from the Clipboard" );
    buttonPanel.add( importButton );
    tablePanel.add( buttonPanel, BorderLayout.SOUTH );

    // Device Parameter Table
    deviceModel = new ParameterTableModel( protocol, ParameterTableModel.Type.DEVICE );

    deviceTable = new JTableX( deviceModel );
    SpinnerCellEditor editor = new SpinnerCellEditor( 0, 8, 1 );
    new TextPopupMenu(( JTextField )(( DefaultCellEditor )deviceTable.getDefaultEditor( String.class )).getComponent());
    deviceTable.setDefaultEditor( Integer.class, editor );
    JScrollPane scrollPane = new JScrollPane( deviceTable );
    tablePanel = new JPanel( new BorderLayout());
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Device Parameters" ));
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 7, 3, 7" );
    Dimension d = deviceTable.getPreferredScrollableViewportSize();
    d.height = deviceTable.getRowHeight() * 4;
    deviceTable.setPreferredScrollableViewportSize( d );

    label = new JLabel( "Raw Fixed Data:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 9" );
    rawHexData = new JTextField();
    new TextPopupMenu( rawHexData );
    mainPanel.add( rawHexData, "3, 9" );

    // Command Parameter table
    commandModel = new ParameterTableModel( protocol, ParameterTableModel.Type.COMMAND );

    commandTable = new JTableX( commandModel );
    commandTable.setDefaultEditor( Integer.class, editor );
    new TextPopupMenu(( JTextField )(( DefaultCellEditor )commandTable.getDefaultEditor( String.class )).getComponent());
    scrollPane = new JScrollPane( commandTable );
    tablePanel = new JPanel( new BorderLayout());
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Command Parameters" ));
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 11, 3, 11" );
    d = commandTable.getPreferredScrollableViewportSize();
    d.height = commandTable.getRowHeight() * 4;
    commandTable.setPreferredScrollableViewportSize( d );

    buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));

    view = new JButton( "View Ini" );
    view.setToolTipText( "View the protocols.ini entry for this protocol." );
    view.addActionListener( this );
    view.setEnabled( false );
    buttonPanel.add( view );

    buttonPanel.add( Box.createHorizontalGlue());

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    buttonPanel.add( ok );

    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    buttonPanel.add( cancel );

    contentPane.add( buttonPanel, BorderLayout.SOUTH );

    Hex id = protocol.getID();
    pid.setValue( id );
    rawHexData.setText( protocol.getFixedData( new Value[ 0 ]).toString());

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
  }

  private MaskFormatter createMaskFormatter( String mask )
  {
    MaskFormatter f = null;
    try
    {
      f = new MaskFormatter( "HH HH" );
    }
    catch (ParseException e)
    {
      e.printStackTrace( System.err );
    }
    return f;
  }

  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == importButton )
    {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable clipData = clipboard.getContents( clipboard );
      if ( clipData != null )
      {
        try
        {
          if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
          {
            String s =
              ( String )( clipData.getTransferData( DataFlavor.stringFlavor ));
            importProtocolCode( s );
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace( System.err );
        }
      }
    }
    else if ( source == view )
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      try
      {
        pw.println( "[" + protocol.getName() + "]" );
        pw.println( "PID=" + protocol.getID() );
        protocol.store( new PropertyWriter( pw ));
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
      JTextArea ta = new JTextArea( sw.toString(), 10, 70 );
      new TextPopupMenu( ta );
      ta.setEditable( false );
      JOptionPane.showMessageDialog( this, new JScrollPane( ta ), "Protocol.ini entry text", JOptionPane.PLAIN_MESSAGE );
    }
    else if ( source == ok )
    {
      userAction = JOptionPane.OK_OPTION;
      setVisible( false );
      dispose();
    }
    else if ( source == cancel )
    {
      userAction = JOptionPane.CANCEL_OPTION;
      setVisible( false );
      dispose();
    }
  }

  public ManualProtocol getProtocol()
  {
    if ( userAction != JOptionPane.OK_OPTION )
      return null;

//    protocol.setDeviceParms( deviceParms );
//    protocol.setDeviceTranslators( deviceTranslators );
//    protocol.setCommandParms( cmdParms );
//    protocol.setCommandTranslators( cmdTranslators );
    protocol.setRawHex( new Hex( rawHexData.getText()));

    return protocol;
  }

  // PropertyChangeListener methods
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    Hex id = ( Hex )pid.getValue();
    boolean flag = ( id != null ) && protocol.hasAnyCode();
    ok.setEnabled( flag );
    view.setEnabled( flag );
    if ( source == pid )
      protocol.setID( id );
  }

  // DocumentListener methods
  public void documentChanged( DocumentEvent e )
  {
    String text = name.getText();
    protocol.setName( text );
  }
  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }
  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }
  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  public class CodeTableModel
    extends AbstractTableModel
  {
    public int getRowCount(){ return procs.length; }
    public int getColumnCount(){ return colNames.length; }
    public String getColumnName( int col ){ return colNames[ col ]; }
    public Class getColumnClass( int col ){ return classes[ col ]; }
    public boolean isCellEditable( int row, int col ){ return ( col == 1 ); }
    public Object getValueAt( int row, int col )
    {
      if ( col == 0 )
        return procs[ row ];
      else
        return protocol.getCode( procs[ row ]);
    }
    public void setValueAt( Object value, int row, int col )
    {
      if ( col == 1 )
      {
        Hex newCode = ( Hex )value;
        if (( newCode != null ) && ( newCode.length() != 0 ))
        {
          if ( !protocol.hasAnyCode())
          {
            int fixedDataLength = Protocol.getFixedDataLengthFromCode( procs[ row ].getEquivalentName(), newCode );
            ArrayList< Value > devParms = new ArrayList< Value >();
            Value zero = new Value( 0 );
            for ( int i = 0; i < fixedDataLength; ++i )
              devParms.add( zero );
            int cmdLength = Protocol.getCmdLengthFromCode( procs[ row ].getEquivalentName(), newCode );
            protocol.createDefaultParmsAndTranslators( cmdLength << 4, false, false,
                         8, devParms, new short[ 0 ], 8 );
            deviceModel.fireTableDataChanged();
            commandModel.fireTableDataChanged();
          }
        }
        protocol.setCode(( Hex )value, procs[ row ] );
        fireTableRowsUpdated( row, row );
      }
    }
  }

  private void importProtocolCode( String string )
  {
    StringTokenizer st = new StringTokenizer( string, "\n" );
    String text = null;
    String processor = null;
    while( st.hasMoreTokens())
    {
      while ( st.hasMoreTokens())
      {
        text = st.nextToken().toUpperCase();
        System.err.println( "got '" + text );
        if ( text.startsWith( "UPGRADE PROTOCOL 0 =" ))
        {
          StringTokenizer st2 = new StringTokenizer( text, "()=" );
          st2.nextToken(); // discard everything before the =
          String pidStr = st2.nextToken().trim();
          System.err.println( "Imported pid is " + pidStr );
          processor = st2.nextToken().trim();
          System.err.println( "processorName is " + processor );
          if ( processor.startsWith( "S3C8" ))
            processor = "S3C80";
          if ( st2.hasMoreTokens())
          {
            String importedName = st2.nextToken().trim();
            System.err.println( "importedName is " + importedName );
          }
          break;
        }
      }
      if ( st.hasMoreTokens())
      {
        text = st.nextToken(); // 1st line of code
        while ( st.hasMoreTokens())
        {
          String temp = st.nextToken();
          if ( temp.equals( "End" ))
            break;
          text = text + ' ' + temp;
        }
        System.err.println( "getting processor with name " + processor );
        Processor p = ProcessorManager.getProcessor( processor );
        if ( p != null )
          processor = p.getFullName();
        System.err.println( "Adding code for processor " + processor );
        System.err.println( "Code is "  + text );
        for ( int i = 0; i < procs.length; i++ )
        {
          if ( procs[ i ] == p )
            codeModel.setValueAt( new Hex( text ), i, 1 );
        }
      }
    }
  }

  private ManualProtocol protocol = null;

  private CodeTableModel codeModel = null;
  private JTableX codeTable = null;
  private ParameterTableModel deviceModel = null;
  private JTableX deviceTable = null;
  private ParameterTableModel commandModel = null;
  private JTableX commandTable = null;

  private JTextField name = null;
  private JFormattedTextField pid = null;
  private JTextField rawHexData = null;

  private JButton importButton = null;

  private JButton view = null;
  private JButton ok = null;
  private JButton cancel = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
//  private final static Object[] typeChoices = { "Numeric entry", "Drop-down list", "Check-box" };
  private final static Object[] bitChoices = { "8", "7", "6", "5", "4", "3", "2", "1" };
  private final static Object[] styleChoices = { "MSB", "MSB-Comp", "LSB", "LSB-Comp" };
  private final static String[] colNames = { "Processor", "Protocol Code" };
  private final static Class[] classes = { Processor.class, Hex.class };
  private static Processor[] procs = new Processor[ 0 ];


}
