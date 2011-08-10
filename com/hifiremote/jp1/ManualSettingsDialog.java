package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;

import com.hifiremote.jp1.assembler.CommonData;

// TODO: Auto-generated Javadoc
/**
 * The Class ManualSettingsDialog.
 */
public class ManualSettingsDialog extends JDialog implements ActionListener, PropertyChangeListener, DocumentListener,
    ChangeListener, ListSelectionListener
{

  /**
   * Instantiates a new manual settings dialog.
   * 
   * @param owner
   *          the owner
   * @param protocol
   *          the protocol
   */
  public ManualSettingsDialog( JDialog owner, ManualProtocol protocol )
  {
    super( owner, "Manual Settings", true );
    createGui( owner, protocol );
  }

  /**
   * Instantiates a new manual settings dialog.
   * 
   * @param owner
   *          the owner
   * @param protocol
   *          the protocol
   */
  public ManualSettingsDialog( JFrame owner, ManualProtocol protocol )
  {
    super( owner, "Manual Settings", true );
    createGui( owner, protocol );
  }

  public class CodeCellRenderer extends DefaultTableCellRenderer
  {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int col ) 
    {
      Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
      CodeTableModel model = ( CodeTableModel )table.getModel();
      if ( isSelected )
      {
        c.setForeground( ( Boolean )model.getValueAt( row, 2 ) ? Color.YELLOW : Color.WHITE );
      }
      else
      {
        c.setForeground( ( Boolean )model.getValueAt( row, 2 ) ? Color.GRAY : Color.BLACK );
      }
      return c;
    }
  }
  
  /**
   * Creates the gui.
   * 
   * @param owner
   *          the owner
   * @param protocol
   *          the protocol
   */
  private void createGui( Component owner, ManualProtocol protocol )
  {
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();
    double scale = 0.75;
    
    JPanel leftPanel = new JPanel( new BorderLayout() );
    JPanel rightPanel = new JPanel( new BorderLayout() );
    rightPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

    this.protocol = protocol;
    System.err.println( "protocol=" + protocol );

    double b = 5; // space between rows and around border
    double c = 10; // space between columns
    double pr = TableLayout.PREFERRED;
    double pf = TableLayout.FILL;
    double size1[][] =
    {
        {
            b, pr, c, pf, b
        }, // cols
        {
            b, pr, b, pr, b, pr, b
        }  // rows
    };
    
    double size2[][] =
    {
        {
            b, pr, c, pf, b
        }, // cols
        {
            b, pr, b, pr, b, pr, b, pr, b
        }  // rows
    };
    
    double size3[][] =
    {
        {
            b, pr, c, pf, b, pr, b
        }, // cols
        {
            b, pr, pr, pr, c, pr, pr, pr, pr, c, pr, pr, pr, pr,
            c, pr, pr, pr, /*c, pr, pr, c, pr, pr, c, pr, pr,*/ c, pr, pr,
            c, pr, pr, pr, c, pr, pr, pr, pr, pr, c, pr, pr, pr,
            c, pr, pr, pr, pr, /*c, pr,*/ b      
        }  // rows
    };
    
    JSplitPane outerPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel );
    outerPane.setResizeWeight( 0 );
    contentPane.add( outerPane, BorderLayout.CENTER );
    
    JPanel mainPanel = new JPanel( new TableLayout( size1 ) );
    leftPanel.add( mainPanel, BorderLayout.PAGE_START );
    
    JLabel label = new JLabel( "Name:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 1" );
    name = new JTextField( protocol.getName() );
    name.setEditable( false );
    name.setEnabled( false );
    name.getDocument().addDocumentListener( this );
    mainPanel.add( name, "3, 1" );

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 3" );

    pid = new JFormattedTextField( new HexFormat( 2, 2 ) );
    new TextPopupMenu( pid );
    pid.addPropertyChangeListener( "value", this );
    mainPanel.add( pid, "3, 3" );

    // Protocol Code Table
    JPanel tablePanel = new JPanel( new BorderLayout() );
    mainPanel.add( tablePanel, "1, 5, 3, 5" );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Protocol code" ) );
    codeModel = new CodeTableModel();
    codeTable = new JTableX( codeModel );
    tablePanel.add( new JScrollPane( codeTable ), BorderLayout.CENTER );
    DefaultTableCellRenderer r = ( DefaultTableCellRenderer )codeTable.getDefaultRenderer( String.class );
    r.setHorizontalAlignment( SwingConstants.CENTER );
    codeTable.setDefaultEditor( Hex.class, new HexCodeEditor() );   
    codeTable.getSelectionModel().addListSelectionListener( this );//new ListSelectionListener()

    JLabel l = ( JLabel )codeTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent( codeTable,
        colNames[ 0 ], false, false, 0, 0 );

    TableColumnModel columnModel = codeTable.getColumnModel();
    TableColumn column = columnModel.getColumn( 0 );
    int width = l.getPreferredSize().width;

    procs = ProcessorManager.getProcessors();
    int count = 0;
    for ( int i = 0; i < procs.length; i++ )
    {
      Processor proc = procs[ i ];
      if ( proc.getEquivalentName().equals( proc.getFullName() ) )
        ++count;
    }
    Processor[] uProcs = new Processor[ count ];
    count = 0;
    for ( int i = 0; i < procs.length; i++ )
    {
      Processor proc = procs[ i ];
      if ( proc.getEquivalentName().equals( proc.getFullName() ) )
        uProcs[ count++ ] = proc;
    }
    procs = uProcs;
    for ( int i = 0; i < procs.length; i++ )
    {
      l.setText( procs[ i ].getFullName() );
      width = Math.max( width, l.getPreferredSize().width );
    }
    for ( int i = 0; i < procs.length; i++ )
    {
      column.setMinWidth( width );
      column.setMaxWidth( width );
      column.setPreferredWidth( width );
    }
    
    columnModel.getColumn( 1 ).setCellRenderer( new CodeCellRenderer() );
    
    codeTable.doLayout();    
    Dimension d = codeTable.getPreferredSize();
    d.width = (int)(d.width * scale );
    codeTable.setPreferredScrollableViewportSize( d );

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    importButton = new JButton( "Import Protocol Upgrade" );
    importButton.addActionListener( this );
    importButton.setToolTipText( "Import Protocol Upgrades(s) from the Clipboard" );
    importButton.setEnabled( false );
    buttonPanel.add( importButton );
    
    JPanel messagePanel = new JPanel( new FlowLayout( FlowLayout.LEFT) );
    messagePanel.add( messageLabel );
    setMessage( 2 );
    
    JPanel midPanel = new JPanel( new BorderLayout() );
    midPanel.add( buttonPanel, BorderLayout.CENTER );
    midPanel.add( messagePanel, BorderLayout.PAGE_END );
    tablePanel.add( midPanel, BorderLayout.PAGE_END) ;
  
    // Create lower part of left panel as tabbed pane
    mainPanel = new JPanel( new TableLayout( size2 ) );
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab( "Device Data", mainPanel );
    leftPanel.add( tabbedPane, BorderLayout.CENTER);
    
    // Device Parameter Table on Device Data tab
    deviceModel = new ParameterTableModel( protocol, ParameterTableModel.Type.DEVICE );
    deviceTable = new JTableX( deviceModel );
    SpinnerCellEditor editor = new SpinnerCellEditor( 0, 8, 1 );
    new TextPopupMenu( ( JTextField )( ( DefaultCellEditor )deviceTable.getDefaultEditor( String.class ) )
        .getComponent() );
    deviceTable.setDefaultEditor( Integer.class, editor );
    JScrollPane scrollPane = new JScrollPane( deviceTable );
    tablePanel = new JPanel( new BorderLayout() );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Device Parameters" ) );
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 1, 3, 1" );
    d = deviceTable.getPreferredScrollableViewportSize();
    d.height = deviceTable.getRowHeight() * 4;
    d.width = (int)(d.width * scale );
    deviceTable.setPreferredScrollableViewportSize( d );

    label = new JLabel( "Default Fixed Data:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 3" );
    rawHexData = new JTextField();
    rawHexData.getDocument().addDocumentListener( this );
    new TextPopupMenu( rawHexData );
    mainPanel.add( rawHexData, "3, 3" );

    // Command Parameter table on Device Data tab
    commandModel = new ParameterTableModel( protocol, ParameterTableModel.Type.COMMAND );

    commandTable = new JTableX( commandModel );
    commandTable.setDefaultEditor( Integer.class, editor );
    new TextPopupMenu( ( JTextField )( ( DefaultCellEditor )commandTable.getDefaultEditor( String.class ) )
        .getComponent() );
    scrollPane = new JScrollPane( commandTable );
    tablePanel = new JPanel( new BorderLayout() );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Command Parameters" ) );
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 5, 3, 5" );
    d = commandTable.getPreferredScrollableViewportSize();
    d.height = commandTable.getRowHeight() * 4;
    d.width = (int)(d.width * scale );
    commandTable.setPreferredScrollableViewportSize( d );

    label = new JLabel( "Command Index:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 7" );
    cmdIndex = new JSpinner( new SpinnerNumberModel( protocol.getCmdIndex(), 0, protocol.getDefaultCmd().length() - 1,
        1 ) );
    cmdIndex.addChangeListener( this );
    mainPanel.add( cmdIndex, "3, 7" );
    
    // Protocol Data tab of lower left panel
    mainPanel = new JPanel( new TableLayout( size3 ) );
    scrollPane = new JScrollPane( mainPanel );
    scrollPane.setPreferredSize( scrollPane.getPreferredSize() );
    tabbedPane.addTab( "Protocol Data", scrollPane );
    
    for ( int i = 0; i < dataComponents.length; i++ )
    {
      if ( dataComponents[ i ][ 0 ] != null )
      {
        if ( dataComponents[ i ].length > 1 )
        {
          label = ( JLabel )dataComponents[ i ][ 1 ];
        }
        else
        {
          label = new JLabel();
        }
        label.setText( dataLabels[ i ] );
        mainPanel.add( label, "1, " + i );
        mainPanel.add( dataComponents[ i ][ 0 ], "3, " + i );
        if ( i < dataSuffixes.length && dataSuffixes[ i ] != null )
        {
          mainPanel.add( new JLabel( dataSuffixes[ i ] ), "5, " + i );
        }
      }
    }
    
    populateComboBox( devBytes, CommonData.to15 );
    populateComboBox( cmdBytes, CommonData.to15 );

    // Disassembly on right pane   
    assemblerTable = new JP1Table( assemblerModel );
    assemblerTable.initColumns( assemblerModel );
    assemblerModel.dialog = this;
    scrollPane = new JScrollPane( assemblerTable );
    scrollPane.setBorder( BorderFactory.createTitledBorder( "Disassembly" ) );
    rightPanel.add( scrollPane, BorderLayout.CENTER );
    
    // Button Panel
    buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

    view = new JButton( "View Ini" );
    view.setToolTipText( "View the protocols.ini entry for this protocol." );
    view.addActionListener( this );
    view.setEnabled( false );
    buttonPanel.add( view );

    buttonPanel.add( Box.createHorizontalGlue() );

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    buttonPanel.add( ok );

    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    buttonPanel.add( cancel );

    leftPanel.add( buttonPanel, BorderLayout.SOUTH );

    Hex id = protocol.getID();
    pid.setValue( id );
    rawHexData.setText( protocol.getFixedData( new Value[ 0 ] ).toString() );

    d = rightPanel.getPreferredSize();
    d.width = (int)(leftPanel.getPreferredSize().width * 0.90 / scale );
    rightPanel.setPreferredSize( d );
    
    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
    
    isEmpty = new boolean[ procs.length ];
    for ( int i = 0; i < procs.length; i++ )
    {
      Hex hex = protocol.getCode( procs[ i ] );
      isEmpty[ i ] = ( hex == null || hex.length() == 0 );
    }
  }
  
  public void setForCustomCode()
  {
//    pid.setEditable( false );
//    pid.setEnabled( false );
    
    deviceTable.setEnabled( false );
    deviceTable.setForeground( Color.GRAY );

    commandTable.setEnabled( false );
    commandTable.setForeground( Color.GRAY );

    rawHexData.setEnabled( false );
    cmdIndex.setEnabled( false );
    
    tabbedPane.setSelectedIndex( 1 );
  
    enableButtons();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
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
          if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ) )
          {
            String s = ( String )( clipData.getTransferData( DataFlavor.stringFlavor ) );
            importProtocolCode( s );
          }
        }
        catch ( Exception ex )
        {
          ex.printStackTrace( System.err );
        }
      }
      if ( codeTable.getSelectedRowCount() == 1 )
      {
        assemblerModel.disassemble( protocol, procs[ codeTable.getSelectedRow() ] );
      }
    }
    else if ( source == view )
    {
      JTextArea ta = new JTextArea( protocol.getIniString( false, pid.isEnabled() ), 10, 70 );
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

  /**
   * Gets the protocol.
   * 
   * @return the protocol
   */
  public ManualProtocol getProtocol()
  {
    if ( userAction != JOptionPane.OK_OPTION )
      return null;

    // protocol.setDeviceParms( deviceParms );
    // protocol.setDeviceTranslators( deviceTranslators );
    // protocol.setCommandParms( cmdParms );
    // protocol.setCommandTranslators( cmdTranslators );
    protocol.setRawHex( new Hex( rawHexData.getText() ) );

    return protocol;
  }

  // PropertyChangeListener methods
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == pid )
    {
      Hex id = ( Hex )pid.getValue();
      boolean inDeviceUpgrade = false;
      Protocol p = null;
      DeviceUpgrade du = null;
      if ( id != null && id.length() != 0 && remoteConfig != null )
      {
        Remote remote = remoteConfig.getRemote();
        for ( DeviceUpgrade temp : remoteConfig.getDeviceUpgrades() )
        {
          du = temp;
          p = temp.getProtocol();
          
          if ( p.getID( remote ).equals( id ) )
          {
            inDeviceUpgrade = true;
            break;
          }
        }
        if ( inDeviceUpgrade )
        {
          String title = "Manual Settings";
          boolean exit = false;
          String starredID = du.getStarredID();
          boolean usesProtocolUpgrade = ( starredID.endsWith( "*" ) );

          if ( usesProtocolUpgrade )
          {
            String message = "There is a Device Upgrade that is using a protocol upgrade with\n"
                           + "PID " + id + " Do you want to abort this PID choice and enter\n"
                           + "a different one?  If so, please press OK.\n\n"
                           + "If you want to edit that protocol code, also press OK, then exit\n"
                           + "this dialog, change to the Devices page and edit the protocol of\n"
                           + "the device upgrade from there.\n\n"
                           + "To continue, press CANCEL but you will be creating a Manual Protocol\n"
                           + "that cannot be accessed while that Device Upgrade is present.";
            exit = ( JOptionPane.showConfirmDialog( null, message, title, 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.OK_OPTION );
          }
          else
          {
            String message = "There is a Device Upgrade with protocol with PID " + id + " that\n"
                           + "is not yet using a protocol upgrade, so you cannot create a new\n"
                           + "manual protocol with that PID.  If you want to create a manual\n"
                           + "protocol then please choose a different PID.  If you want to\n"
                           + "provide code for that device upgrade, please change to the\n"
                           + "Devices page and edit the protocol from there.";
            JOptionPane.showMessageDialog( null, message, title, JOptionPane.WARNING_MESSAGE );
            exit = true;
          }
          
          if ( exit )
          {
            pid.setValue( null );
            enableButtons();
            return;
          }         
        }
      }
      protocol.setID( id );
    }
    enableButtons();
  }

  protected void enableButtons()
  {
    if ( deviceTable.isEnabled() )
    {
      // Normal Manual Settings usage
      Hex id = ( Hex )pid.getValue();
      boolean flag = ( id != null ) && ( id.length() != 0 ) && protocol.hasAnyCode();
      ok.setEnabled( flag );
      view.setEnabled( flag );
    }
    else
    {
      // Custom Code usage
      ok.setEnabled( true );
      view.setEnabled( false );
    }
  }

  // DocumentListener methods
  /**
   * Document changed.
   * 
   * @param e
   *          the e
   */
  public void documentChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();

    if ( doc == name.getDocument() )
    {
      protocol.setName( name.getText() );
    }
    else if ( doc == rawHexData.getDocument() )
    {
      protocol.setRawHex( new Hex( rawHexData.getText() ) );
    }
    enableButtons();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /**
   * The Class CodeTableModel.
   */
  public class CodeTableModel extends AbstractTableModel
  {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
      return procs.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
      return colNames.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName( int col )
    {
      return colNames[ col ];
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class< ? > getColumnClass( int col )
    {
      return classes[ col ];
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable( int row, int col )
    {
      if ( displayRemote != null && !procs[ row ].getEquivalentName().equals( displayRemote.getProcessor().getEquivalentName() ) )
      {
        return false;
      }
      return ( col == 1 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int row, int col )
    {
      Hex hex = protocol.getCode( procs[ row ] );
      Hex dispHex = ( displayProtocol == null ) ? null : displayProtocol.getCode( procs[ row ] );
      if ( dispHex == null )
      {
        dispHex = new Hex();
      }
      switch ( col )
      {
        case 0:
          return procs[ row ];
        case 1:
          return ( hex == null || hex.length() == 0 ) ? dispHex : hex;
        default:
          // There are no other columns but this value is used by cell renderer
          return hex == null || hex.length() == 0 || isEmpty[ row ] && hex.equals( dispHex );
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt( Object value, int row, int col )
    {
      if ( col == 1 )
      {
        Hex newCode = ( Hex )value;
        if ( ( newCode != null ) && ( newCode.length() != 0 ) )
        {
          if ( !protocol.hasAnyCode() )
          {
            int fixedDataLength = Protocol.getFixedDataLengthFromCode( procs[ row ].getEquivalentName(), newCode );
            rawHexData.setText( Hex.toString( new short[ fixedDataLength ] ) );
            ArrayList< Value > devParms = new ArrayList< Value >();
            Value zero = new Value( 0 );
            for ( int i = 0; i < fixedDataLength; ++i )
              devParms.add( zero );
            int cmdLength = Protocol.getCmdLengthFromCode( procs[ row ].getEquivalentName(), newCode );
            DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
            for ( int i = 0; i < cmdLength; ++i )
            {
              comboModel.addElement( importButton );
            }
            SpinnerNumberModel spinnerModel = ( SpinnerNumberModel )cmdIndex.getModel();
            spinnerModel.setMaximum( cmdLength - 1 );
            protocol.createDefaultParmsAndTranslators( cmdLength << 4, false, false, 8, devParms, new short[ 0 ], 8 );
            deviceModel.fireTableDataChanged();
            commandModel.fireTableDataChanged();
          }
        }
        else if ( codeWhenNull != null )
        {
          String title = "Code deletion";
          String message = "This protocol is not built in to the remote.  Do you want to restore\n"
                         + "the code to the standard code for this protocol?\n\n"
                         + "If you select NO then the protocol upgrade for this device upgrade\n"
                         + "will be deleted.  The device upgrade will not function until you\n"
                         + "restore the protocol upgrade, which you may do by deleting this\n"
                         + "null entry and answering this question again.";
          boolean restore = ( JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION );
          if ( restore )
          {
            value = codeWhenNull;
          }
          else
          {
            value = new Hex();
          }
        }
        protocol.setCode( ( Hex )value, procs[ row ] );
        fireTableRowsUpdated( row, row );
        enableButtons();
        assemblerModel.disassemble( protocol, procs[ row ] );
      }
    }
  }

  /**
   * Import protocol code.
   * 
   * @param string
   *          the string
   */
  private void importProtocolCode( String string )
  {
    StringTokenizer st = new StringTokenizer( string, "\n" );
    String text = null;
    String processor = null;
    String pidStr = null;
    while ( st.hasMoreTokens() )
    {
      while ( st.hasMoreTokens() )
      {
        text = st.nextToken().toUpperCase();
        System.err.println( "got '" + text );
        if ( text.startsWith( "UPGRADE PROTOCOL 0 =" ) )
        {
          StringTokenizer st2 = new StringTokenizer( text, "()=" );
          st2.nextToken(); // discard everything before the =
          pidStr = st2.nextToken().trim();
          System.err.println( "Imported pid is " + pidStr );
          processor = st2.nextToken().trim();
          System.err.println( "processorName is " + processor );
          if ( processor.startsWith( "S3C8" ) )
            processor = "S3C80";
          else if ( processor.startsWith( "S3F8" ) )
            processor = "S3F80";
          if ( st2.hasMoreTokens() )
          {
            String importedName = st2.nextToken().trim();
            System.err.println( "importedName is " + importedName );
          }
          break;
        }
      }
      if ( st.hasMoreTokens() )
      {
        text = st.nextToken(); // 1st line of code
        while ( st.hasMoreTokens() )
        {
          String temp = st.nextToken();
          if ( temp.trim().equals( "End" ) )
            break;
          text = text + ' ' + temp;
        }
        System.err.println( "getting processor with name " + processor );
        Processor p = ProcessorManager.getProcessor( processor );
        if ( p != null )
        {
          // processor = p.getFullName();
          processor = p.getEquivalentName();
          p = ProcessorManager.getProcessor( processor );
          pid.setValue( new Hex( pidStr ) );
        }
        System.err.println( "Adding code for processor " + processor );
        System.err.println( "Code is " + text );
        for ( int i = 0; i < procs.length; i++ )
        {
          if ( procs[ i ] == p )
            codeModel.setValueAt( new Hex( text ), i, 1 );
        }
      }
    }
  }

  /** The protocol. */
  private ManualProtocol protocol = null;
  
  private Protocol displayProtocol = null;
  
  private Remote displayRemote = null;

  /** The code model. */
  private CodeTableModel codeModel = null;

  /** The code table. */
  private JTableX codeTable = null;

  /** The device model. */
  private ParameterTableModel deviceModel = null;

  /** The device table. */
  private JTableX deviceTable = null;

  /** The command model. */
  private ParameterTableModel commandModel = null;

  /** The command table. */
  private JTableX commandTable = null;

  /** The name. */
  private JTextField name = null;

  /** The pid. */
  public JFormattedTextField pid = null;

  /** The raw hex data. */
  private JTextField rawHexData = null;

  private JSpinner cmdIndex = null;

  /** The import button. */
  private JButton importButton = null;

  /** The view. */
  private JButton view = null;

  /** The ok. */
  private JButton ok = null;

  /** The cancel. */
  private JButton cancel = null;
  
  private JTabbedPane tabbedPane = null;
  
  public JTextField frequency = new JTextField();
  public JTextField dutyCycle = new JTextField();
  public JComboBox sigStruct = new JComboBox();
  
  public JComboBox devBytes = new JComboBox();
  public JComboBox devBits1 = new JComboBox();
  public JComboBox devBits2 = new JComboBox();
  public JComboBox devBitDbl = new JComboBox();
  
  public JComboBox cmdBytes = new JComboBox();
  public JComboBox cmdBits1 = new JComboBox();
  public JComboBox cmdBits2 = new JComboBox();
  public JComboBox cmdBitDbl = new JComboBox();
  
  public JTextField rptValue = new JTextField();
  public JComboBox rptType = new JComboBox();
  public JComboBox rptHold = new JComboBox();
  
//  public JComboBox chkByteStyle = new JComboBox();
//  public JTextField bitsHeld = new JTextField();
//  
//  public JComboBox miniCombiner = new JComboBox();
//  public JComboBox sigStyle = new JComboBox();
//  
//  public JTextField vecOffset = new JTextField();
//  public JTextField dataOffset = new JTextField();
  
  public JTextField burst1On = new JTextField();
  public JTextField burst1Off = new JTextField();
  
  public JTextField burst0On = new JTextField();
  public JTextField burst0Off = new JTextField();
  public JComboBox xmit0rev = new JComboBox();
  
  public JComboBox leadInStyle = new JComboBox();
  public JComboBox burstMidFrame = new JComboBox();
  public JTextField afterBits = new JTextField();
  public JTextField leadInOn = new JTextField();
  public JTextField leadInOff = new JTextField();
  
  public JComboBox leadOutStyle = new JComboBox();
  public JTextField leadOutOff = new JTextField();
  public JComboBox offAsTotal = new JComboBox();
  
  public JTextField altLeadOut = new JTextField();
  public JComboBox useAltLeadOut = new JComboBox();
  public JTextField altFreq = new JTextField();
  public JTextField altDuty = new JTextField();
  
//  public JComboBox toggleBit = new JComboBox();
  
  public JLabel devBits1lbl = new JLabel();
  public JLabel devBits2lbl = new JLabel();
  public JLabel cmdBits1lbl = new JLabel();
  public JLabel cmdBits2lbl = new JLabel();
  public JLabel burstMidFrameLbl = new JLabel();
  public JLabel afterBitsLbl = new JLabel();
  public JLabel altFreqLbl = new JLabel();
  public JLabel altDutyLbl = new JLabel();
  
  
  private Component[][] dataComponents = { { null },
      { frequency }, { dutyCycle }, { sigStruct }, { null },
      { devBytes }, { devBits1, devBits1lbl }, { devBits2, devBits2lbl }, { devBitDbl }, { null },
      { cmdBytes }, { cmdBits1, cmdBits1lbl }, { cmdBits2, cmdBits2lbl }, { cmdBitDbl }, { null },
      { rptValue }, { rptType }, { rptHold }, { null },
//      { chkByteStyle }, { bitsHeld }, { null },
//      { miniCombiner }, { sigStyle }, { null },
//      { vecOffset }, { dataOffset }, { null },
      { burst1On }, { burst1Off }, { null },
      { burst0On }, { burst0Off }, { xmit0rev }, { null },
      { leadInStyle }, { burstMidFrame, burstMidFrameLbl }, { afterBits, afterBitsLbl }, { leadInOn }, { leadInOff }, { null },
      { leadOutStyle }, { leadOutOff }, { offAsTotal }, { null },
      { altLeadOut }, { useAltLeadOut }, { altFreq, altFreqLbl }, { altDuty, altDutyLbl }//, { null },
//      { toggleBit }
  };
  
  private String[] dataLabels = { null,
      "Frequency", "Duty Cycle", "Signal Structure", null,
      "Device Bytes", "Bits/Dev1", "Bits/Dev2", "Dev Bit Doubling", null,
      "Command Bytes", "Bits/Cmd1", "Bits/Cmd2", "Cmd Bit Doubling", null,
      "Repeat Value", "Type", "Hold", null,
//      "Check Byte Style", "# Bytes Checked", null,
//      "Mini-Combiner", "Signal Style", null,
//      "Vector Offset", "Data Offset", null,
      "1 Burst ON", "OFF", null,
      "0 Burst ON", "OFF", "Xmit 0 Reversed", null,
      "Lead-In Style", "Burst Mid-Frame", "After # of bits", "Lead-In ON", "OFF", null,
      "Lead-Out Style", "Lead-Out OFF", "OFF as Total", null,
      "Alt Lead-Out", "Use Alt Lead-Out", "Alt Freq", "Alt Duty"//, null,
//      "Toggle Bit"
  };
  
  private String[] dataSuffixes = { null,
      "kHz", "%", null, null,
      null, null, null, null, null,
      null, null, null, null, null,
      null, null, null, null,
//      null, null, null,
//      null, null, null,
//      "bytes", "bytes", null,
      "uSec", "uSec", null,
      "uSec", "uSec", null, null,
      null, null, null, "uSec", "uSec", null,
      null, "uSec", null, null,
      "uSec", null, "kHz", "%"
  };

  /** The user action. */
  private int userAction = JOptionPane.CANCEL_OPTION;
  // private final static Object[] typeChoices = { "Numeric entry", "Drop-down list", "Check-box" };
  /** The Constant colNames. */
  private final static String[] colNames =
  {
      "Processor", "Protocol Code"
  };

  /** The Constant classes. */
  private final static Class< ? >[] classes =
  {
      Processor.class, Hex.class
  };

  /** The procs. */
  private static Processor[] procs = new Processor[ 0 ];

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged( ChangeEvent arg0 )
  {
    if ( protocol.setCmdIndex( ( ( Integer )cmdIndex.getValue() ).intValue() ) )
    {
      commandModel.fireTableDataChanged();
    }

  }
  
  public Hex codeWhenNull = null;
  
  public RemoteConfiguration remoteConfig = null;
  
  private AssemblerTableModel assemblerModel = new AssemblerTableModel();
  private JP1Table assemblerTable = null;
  private boolean isEmpty[] = null;
  private JLabel messageLabel = new JLabel();

  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    if ( !e.getValueIsAdjusting() )
    {
      if ( codeTable.getSelectedRowCount() == 1 )
      {
        Hex hex = protocol.getCode( procs[ codeTable.getSelectedRow() ] );
        Protocol prot = ( ( hex == null || hex.length() == 0 ) && displayProtocol != null ) ?
            displayProtocol : protocol;
        assemblerModel.disassemble( prot, procs[ codeTable.getSelectedRow() ] );
        importButton.setEnabled( codeTable.isCellEditable( codeTable.getSelectedRow(), 1 ) );
      }
      else
      {
        importButton.setEnabled( false );
      }
    }
  }

  public void setDisplayProtocol( Protocol displayProtocol )
  {
    this.displayProtocol = displayProtocol;
  }

  public void setDisplayRemote( Remote displayRemote )
  {
    this.displayRemote = displayRemote;
  }
  
  public void setMessage( int n )
  {
    String text = "<HTML>";
    if ( n == 2 )
    {
      text += "This is a custom protocol, so only the code for the remote's <BR>processor is editable.<BR>";
    }
    text += "Code shown in gray is standard code for information only.</HTML>";
    messageLabel.setText( text );
  }
  
  public void populateComboBox( Component component, Object[] array )
  {
    if ( !( component instanceof JComboBox ) )
    {
      return;
    }
    JComboBox comboBox = ( JComboBox )component;
    ( (DefaultComboBoxModel )comboBox.getModel() ).removeAllElements();
    if ( array == null )
    {
      return;
    }
    for ( int i = 0; i < array.length; i++ )
    {
      comboBox.addItem( array[ i ] );
    }
  }

}


