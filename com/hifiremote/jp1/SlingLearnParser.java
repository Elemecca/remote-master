package com.hifiremote.jp1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class SlingLearnParser
{
  public static void parse( File file, RemoteConfiguration config )
  {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    FileReader reader;
    try
    {
      reader = new FileReader( file );
      XMLEventReader eventReader = factory.createXMLEventReader( reader );

      XMLEventReader filteredReader = factory.createFilteredReader( eventReader, new EventFilter()
      {

        @Override
        public boolean accept( XMLEvent event )
        {
          return !event.isProcessingInstruction();
        }
      } );

      String brandName = null;
      String modelName = null;
      int deviceType = -1;
      List< LearnedSignal > learnedSignals = config.getLearnedSignals();
      while ( filteredReader.hasNext() )
      {
        XMLEvent event = filteredReader.nextEvent();
        int type = event.getEventType();
        if ( type == XMLEvent.START_ELEMENT )
        {
          StartElement element = event.asStartElement();
          String name = null;
          String command = null;
          int keyCode = -1;
          boolean isTested = false;
          String elementName = element.getName().getLocalPart();
          if ( elementName.equals( "learntremote" ) )
          {
            @SuppressWarnings( "unchecked" )
            Iterator< Attribute > attrs = element.getAttributes();
            while ( attrs.hasNext() )
            {
              Attribute attr = attrs.next();
              String localName = attr.getName().getLocalPart();
              String value = attr.getValue().trim();
              if ( localName.equals( "brandName" ) )
              {
                brandName = value;
              }
              else if ( localName.equals( "modelName" ) )
              {
                modelName = value;
              }
              else if ( localName.equals( "deviceType" ) )
              {
                deviceType = Integer.parseInt( value );
              }
            }
            String devButtonName = config.getRemote().getDeviceButtons()[ deviceType ].getName();
            config.setNotes( brandName + " " + devButtonName + " " + modelName );
          }
          else if ( element.getName().getLocalPart().equals( "learntremotekey" ) )
          {
            @SuppressWarnings( "unchecked" )
            Iterator< Attribute > attrs = element.getAttributes();
            while ( attrs.hasNext() )
            {
              Attribute attr = attrs.next();
              String localName = attr.getName().getLocalPart();
              String value = attr.getValue().trim();
              if ( localName.equals( "name" ) )
              {
                name = value;
              }
              else if ( localName.equals( "code" ) )
              {
                keyCode = Integer.parseInt( value );
              }
              else if ( localName.equals( "command" ) )
              {
                command = value.substring( 6 );
              }
              else if ( localName.equals( "isTested" ) )
              {
                isTested = "true".equals( value ) || "1".equals( value );
              }
            }
            LearnedSignal signal = new LearnedSignal( keyCode, deviceType, 0, new Hex( command ), name
                + ( isTested ? "" : " (untested)" ) );
            learnedSignals.add( signal );
          }
        }
      }
    }
    catch ( FileNotFoundException e )
    {
      e.printStackTrace( System.err );
    }
    catch ( XMLStreamException e )
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), e, "XML Parsing Error", JOptionPane.ERROR_MESSAGE );
      e.printStackTrace( System.err );
    }

  }
}
