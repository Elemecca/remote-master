package com.hifiremote.jp1;

import java.awt.Insets;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Enumeration;
import java.util.Vector;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

public class SetupPanel
  extends KMPanel
  implements ActionListener, DocumentListener
{
  public SetupPanel( DeviceUpgrade deviceUpgrade, Vector protocols )
  {
    super( deviceUpgrade );

    protocolHolder = new JPanel( new BorderLayout());
    Border border = BorderFactory.createTitledBorder( "Protocol Parameters" );
    protocolHolder.setBorder( border );

    Insets insets = border.getBorderInsets( protocolHolder );
    double bt = insets.top;
    double bl = insets.left + 10;
    double br = insets.right;
    double bb = insets.bottom;
    double b = 10;       // space around border
    double i = 5;        // space between rows
    double v = 20;       // space between groupings
    double c = 30;       // space between columns
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
      { b, bl, p, b, p, br, c, f, b },                     // cols
      { b, p, v, p, i, p, v, bt, p, bb, f, b }         // rows
    };
    tl = new TableLayout( size );
    setLayout( tl );

    JLabel label = new JLabel( "Setup Code:", SwingConstants.RIGHT );
    add( label, "2, 1" );

    NumberFormatter nf = new NumberFormatter( new DecimalFormat( "0000" ));
    nf.setMinimum( new Integer( 0 ));
    nf.setMaximum( new Integer( 2047 ));
    nf.setValueClass( Integer.class );
    nf.setAllowsInvalid( false );
    nf.setOverwriteMode( true );
    setupCode = new JFormattedTextField( nf );
    label.setLabelFor( setupCode );
    setupCode.setFocusLostBehavior( JFormattedTextField.COMMIT_OR_REVERT );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and 2047) for the device upgrade." );

    add( setupCode, "4, 1" );

    JPanel notesPanel = new JPanel( new BorderLayout());
    notes = new JTextArea();
    notes.setToolTipText( "Enter any notes about this device upgrade." );
    notesPanel.setBorder( BorderFactory.createTitledBorder( "Notes" ));
    notesPanel.add( new JScrollPane( notes ), BorderLayout.CENTER );
    notes.getDocument().addDocumentListener( this );
    add( notesPanel, "7, 1, 7, 10" );

    label = new JLabel( "Protocol:", SwingConstants.RIGHT );
    add( label, "2, 3" );

    protocolList = new JComboBox( protocols );
    protocolList.addActionListener( this );
    label.setLabelFor( protocolList );
    protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
    add( protocolList, "4, 3" );

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    add( label, "2, 5" );

    protocolID = new JTextField();
    label.setLabelFor( protocolID );
    protocolID.setEditable( false );
    protocolID.setToolTipText( "This is the protocol ID that corresponds to the selected protocol." );
    add( protocolID, "4, 5" );

    add( protocolHolder, "1, 7, 5, 9" );
    label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
    add( label, "2, 8" );

    fixedData = new JTextField();
    fixedData.setEditable( false );
    add( fixedData, "4, 8" );

//    protocolList.setSelectedIndex( 0 );
  }

  public void protocolsLoaded( Vector protocols )
  {
    protocolList.setModel( new DefaultComboBoxModel( protocols ));
  }

  public void update()
  {
    updateInProgress = true;
    setupCode.setValue( new Integer( deviceUpgrade.getSetupCode()));
    Protocol p = deviceUpgrade.getProtocol();
    protocolList.setSelectedItem( p );
    notes.setText( deviceUpgrade.getNotes());
    fixedData.setText( p.getFixedData().toString());
    updateInProgress = false;
  }

  public void updateFixedData()
  {
    Protocol p = getProtocol();
    p.initializeParms();
    fixedData.setText( p.getFixedData().toString());
  }

  // ActionListener Methods
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();

    if ( source == protocolList )
    {
      Protocol protocol = ( Protocol )protocolList.getSelectedItem();
      if ( protocol != null && ( currProtocol != protocol ))
      {
        if ( currProtocol != null && !updateInProgress )
          currProtocol.convertFunctions( deviceUpgrade.getFunctions(), protocol );
        currProtocol = protocol;
        protocolID.setText( protocol.getID().toString());
        deviceUpgrade.setProtocol( protocol );
        if ( parameters != null )
        {
          for ( int i = 0; i < parameters.length; i++ )
          {
            remove( parameters[ i ].getLabel());
            JComponent comp = parameters[ i ].getComponent();
            if ( comp.getClass() == JComboBox.class )
              (( JComboBox )comp ).removeActionListener( this );
            else // assume JTextField
              (( JTextField )comp ).getDocument().removeDocumentListener( this );
            remove( comp );
            tl.deleteRow( 8 );
            tl.deleteRow( 8 );
          }
          doLayout();
        }
        parameters = protocol.getDeviceParameters();
        if ( parameters != null )
        {
          int row = 8;
          for ( int i = 0; i < parameters.length; i++ )
          {
            tl.insertRow( row, TableLayout.PREFERRED );
            add( parameters[ i ].getLabel(), "2, " + row );
            JComponent comp = parameters[ i ].getComponent();
            if ( comp.getClass() == JComboBox.class )
              (( JComboBox )comp ).addActionListener( this );
            else // assume JTextField
              (( JTextField )comp ).getDocument().addDocumentListener( this );
            add( comp, "4, " + row );
            row++;
            tl.insertRow( row++, 5 );
          }
          TableLayoutConstraints tlc = tl.getConstraints( protocolHolder );
          remove( protocolHolder );
          add( protocolHolder, tlc );
          doLayout();
        }
        fixedData.setText( protocol.getFixedData().toString());
      }
    }
    else // must be a protocol parameter
      updateFixedData();
  }

  public Protocol getProtocol()
  {
    return ( Protocol )protocolList.getSelectedItem();
  }

  public void commit()
  {
    deviceUpgrade.setSetupCode( Integer.parseInt( setupCode.getText()));
    deviceUpgrade.setNotes( notes.getText());
    Protocol p = getProtocol();
    deviceUpgrade.setProtocol( p );
    for ( int i = 0; i < parameters.length; i++ )
    {
      parameters[ i ].commit();
    }
  }

  private void updateNotes()
  {
    deviceUpgrade.setNotes( notes.getText());
  }

  private void docChanged( DocumentEvent e )
  {
    if ( e.getDocument() == notes.getDocument() )
      updateNotes();
    else
      updateFixedData();

  }

  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public void insertUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  private JFormattedTextField setupCode = null;
  private JRadioButton useEFC = null;
  private JRadioButton useOBC = null;
  private JComboBox protocolList = null;
  private JTextField protocolID = null;
  private JTextArea notes = null;
  private JPanel protocolHolder = null;
  private JTextField fixedData = null;
  private DeviceParameter[] parameters = null;
  private Protocol currProtocol = null;
  private TableLayout tl;
  private boolean updateInProgress = false;
}

