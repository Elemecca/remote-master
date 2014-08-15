package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

public class SegmentPanel extends RMPanel implements ActionListener
{
  private SegmentTableModel model = null;
  private JP1Table table = null;
  private Box infoBox = null;
  private JLabel signatureLabel = new JLabel();  
  private JLabel processorLabel = new JLabel();  
  private JLabel interfaceLabel = new JLabel();  
  private JLabel infoLabel1 = new JLabel( "Values in black: RMIR data displayed" );
  private JLabel infoLabel2 = new JLabel( "Values in blue: Original data displayed" );
  private JLabel infoLabel3 = new JLabel( "Select between them with \"Preserve original data\"" );
  private JLabel infoLabel4 = new JLabel( "    on the Advanced menu." );
  private JButton applyButton = new JButton( "Apply" );
  
  public SegmentPanel()
  {    
    model = new SegmentTableModel();
    table = new JP1Table( model );
    table.setGridColor( Color.lightGray );
    JScrollPane scrollPane = new JScrollPane( table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    table.setPreferredScrollableViewportSize( d );
    add( scrollPane, BorderLayout.CENTER );
    
    infoBox = Box.createVerticalBox();
    infoBox.setAlignmentX( LEFT_ALIGNMENT );
    infoBox.setBorder( BorderFactory.createEmptyBorder( 20, 10, 5, 5 ) );
    add( infoBox, BorderLayout.LINE_END );
    
    infoBox.add( infoLabel1 );
    infoBox.add( infoLabel2 );
    infoBox.add( infoLabel3 );
    infoBox.add( infoLabel4 );
    infoBox.add( Box.createVerticalStrut( 5 ) );
    infoBox.add( signatureLabel );
    infoBox.add( Box.createVerticalStrut( 5 ) );
    infoBox.add( processorLabel );
    infoBox.add( Box.createVerticalStrut( 5 ) );
    infoBox.add( interfaceLabel );
    infoBox.add( Box.createVerticalStrut( 5 ) );
    infoBox.add(  applyButton );
    infoBox.add( Box.createVerticalGlue());
    applyButton.addActionListener( this );
    
    Font boldFont = ( new JLabel() ).getFont().deriveFont( Font.BOLD );
    infoLabel1.setFont( boldFont );
    infoLabel2.setFont( boldFont );
    infoLabel2.setForeground( Color.BLUE );
  }

  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    Remote remote = null;
    if ( remoteConfig != null && remoteConfig.hasSegments() && !( remote = remoteConfig.getRemote() ).isSSD() )
    {
      RemoteMaster rm = remoteConfig.getOwner();
      List< Segment > segments = new ArrayList< Segment >();
      short[] dataToShow = rm.useSavedData() ? remoteConfig.getSavedData() : remoteConfig.getData();
      int base = remote.getBaseAddress();
      
      // first two bytes are checksum, and in XSight remotes next 18 bytes are E2 info
      int pos = remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
      int segLength = 0;
      int segType = 0;
      while ( pos < remote.getEepromSize() && ( segLength = Hex.get( dataToShow, pos ) ) <= remote.getEepromSize() - pos  )
      {
        segType = dataToShow[ pos + 2 ];
        int segFlags = dataToShow[ pos + 3 ];
        Hex segData = new Hex( dataToShow, pos + 4, segLength - 4 );
        Segment seg = new Segment( segType, segFlags, segData );
        seg.setAddress( base + pos );
        segments.add( seg );
        pos += segLength;
      }
      model.set( remoteConfig, segments );
      table.initColumns();
      
      String sig = remoteConfig.getSigString();
      if ( sig == null )
      {
        sig = remote.getSignature();
      }
      signatureLabel.setText( "Signature:  " + sig );
      processorLabel.setText( "Processor:  " + remote.getProcessorDescription() );
      interfaceLabel.setText( "Interface:  " + remote.getInterfaceType() );
    }
  }

  @Override
  public void addRMPropertyChangeListener( PropertyChangeListener l )
  {
    if ( model != null && l != null )
    {
      model.addPropertyChangeListener( l );
    }
  }

  @Override
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    Object source = e.getSource();
    if ( source == applyButton )
    {
      model.updateData();
    }
  }
  
  protected void finishEditing()
  {
    int editRow = table.getEditingRow();
    if ( editRow != -1 )
    {
      TableCellEditor editor = table.getCellEditor( editRow, table.getEditingColumn() );
      if ( !editor.stopCellEditing() )
      {
        editor.cancelCellEditing();
      }
    }
  }
}
