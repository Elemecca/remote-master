package com.hifiremote.jp1;

import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class RemoteConfiguration
{
  public RemoteConfiguration( File file )
    throws IOException
  {
    BufferedReader in = new BufferedReader( new FileReader( file ));
    String line = in.readLine();
    StringTokenizer st = new StringTokenizer( line, ": " );
    int baseAddr = Integer.parseInt( st.nextToken(), 16 );
    short[] first = new short[ st.countTokens()];
    for ( int i = 0; i < first.length; ++i )
      first[ i ] = Short.parseShort( st.nextToken(), 16 );

    char[] sig = new char[ 8 ];
    for ( int i = 0; i < sig.length; ++i )
      sig[ i ] = ( char )first[ i + 2 ];

    String signature = new String( sig );
    RemoteManager rm = RemoteManager.getRemoteManager();
    Remote[] remotes = rm.findRemoteBySignature( signature );
    if ( remotes.length == 0 )
    {
      for ( int i = 0; i < sig.length; ++i )
        sig[ i ] = ( char )first[ i ];
      remotes = rm.findRemoteBySignature( signature );
    }
    remote = ( Remote )remotes[ 0 ];
    remote.load();

    if ( remote.getBaseAddress() != baseAddr )
      throw new IOException( "BaseAddress of " + file + " doesn't match baseAddress in RDF." );
    
    data = new short[ remote.getEepromSize()];

    for ( int i = 0; i < first.length; ++i )
      data[ i ] = first[ i ];
    first = null;
    while ((  line = in.readLine()) != null )
    {
      if ( line.length() == 0 )
        continue;
      if ( line.equals( "[Notes]" ))
        break;
      st = new StringTokenizer( line, ": " );
      int offset = Integer.parseInt( st.nextToken(), 16 ) - baseAddr;
      while ( st.hasMoreTokens())
        data[ offset++ ] = Short.parseShort( st.nextToken(), 16 );  
    }
    
    savedData = new short[ data.length ];
    System.arraycopy( data, 0, savedData, 0, data.length );

    Vector<String> advNotes = new Vector<String>();
    Vector<String> favNotes = new Vector<String>();
    Vector<String> deviceNotes = new Vector<String>();
    Vector<String> protocolNotes = new Vector<String>();
    Vector<String> learnedNotes = new Vector<String>();
    while (( line = in.readLine()) != null )
    {
      if ( line.length() == 0 )
        continue;
      if ( line.charAt( 0 ) == '[' )
        break;
      int pos = line.indexOf( '=' );
      String temp = line.substring( 0, pos );
      int base = 10;
      if ( temp.charAt( 0 ) == '$' )
      {
        base = 16;
        temp = temp.substring( 1 );
      }
      int index = Integer.parseInt( temp, base );
      int flag = index >> 12;
      index &= 0x0FFF;
      String text = importNotes( line.substring( pos + 1 ));
      Vector< String > v = null;
      if ( flag == 0 )
      {
        notes = text;
        continue;
      }
      else if ( flag == 1 )
        v = advNotes;
      else if ( flag == 2 )
        v = favNotes;
      else if ( flag == 3 )
        v = deviceNotes;
      else if ( flag == 4 )
        v = protocolNotes;
      else if ( flag == 5 )
        v = learnedNotes;

      for ( int j = v.size(); j < index; ++j )
        v.add( "" );
      v.add( text );
    }
    
    decodeLearnedSignals( learnedNotes );
    
    if ( file.getName().toLowerCase().endsWith( ".ir" ))
    {
      decodeUpgrades( deviceNotes, protocolNotes );
      decodeAdvancedCodes( advNotes );
      return;
    }

    Property property = null;
    PropertyReader pr = new PropertyReader( in );
//  if ( "[Device Upgrade]".equals( line ))
    if (( line != null ) && ( line.charAt( 0 ) == '[' ))
      property = new Property( line, "" );
    
    String className = null;
    Properties props = new Properties();
    while ( true )
    {
      if (( property == null ) || ( property.name.charAt( 0 ) == '[' ))
      {
        if ( className != null )
        {
          if ( className.equals( "KeyMove" ))
          {
            KeyMove keyMove = new KeyMove( props );
            keymoves.add(  keyMove );
          }
          else if ( className.equals( "KeyMoveEFC" ))
          {
            KeyMoveEFC keyMove = new KeyMoveEFC( props );
            keymoves.add( keyMove );
          }
          else if ( className.equals( "KeyMoveEFC5" ))
          {
            KeyMoveEFC5 keyMove = new KeyMoveEFC5( props );
            keymoves.add( keyMove );
          }
          else if ( className.equals( "KeyMoveKey" ))
          {
            KeyMoveKey keyMove = new KeyMoveKey( props );
            keymoves.add( keyMove );
          }
          else if ( className.equals( "Macro" ))
          {
            Macro macro = new Macro( props );
            macros.add( macro );
          }
          else if ( className.equals( "DeviceUpgrade" ))
          {
            DeviceUpgrade upgrade = new DeviceUpgrade();
            upgrade.load( props );
            devices.add( upgrade );
          }
          props.clear();
        }
        if ( property != null )
          className = property.name.substring( 1, property.name.length() - 1 );
        else
          break;
      }
      else
        props.setProperty( property.name, property.value );
      property = pr.nextProperty( property );
    }
    in.close();
  }
  
  private DeviceUpgrade findDeviceUpgrade( DeviceButton deviceButton )
  {
    return findDeviceUpgrade( deviceButton.getDeviceTypeIndex( data ), 
                              deviceButton.getSetupCode( data ));
  }

  private DeviceUpgrade findDeviceUpgrade( int deviceTypeIndex, int setupCode )
  {
    for ( DeviceUpgrade deviceUpgrade : devices )
    {
      if (( deviceTypeIndex == deviceUpgrade.getDeviceType().getNumber()) &&
          ( setupCode == deviceUpgrade.getSetupCode()))
        return deviceUpgrade;
    }
    return null;
  }
  
  public RemoteConfiguration( Remote remote )
  {
    this.remote = remote;
    data = new short[ remote.getEepromSize()];
  }
  
  public void parseData()
  {
    Vector< String > v = new Vector< String >();
    decodeUpgrades( v, v );
    decodeAdvancedCodes( v );    
    decodeLearnedSignals( v );
  }

  private void decodeAdvancedCodes( Vector< String > notes )
  {
    Enumeration< String > notesEnum = notes.elements();
    AddressRange advCodeRange = remote.getAdvanceCodeAddress();
    int offset = advCodeRange.getStart();
    int endOffset = advCodeRange.getEnd();
    int count = 0;
    while ( offset <= endOffset )
    {
      short keyCode = data[ offset++ ];
      if ( keyCode == remote.getSectionTerminator())
        break;

      int boundDeviceIndex = 0;
      boolean isMacro = false;
      int length = 0;
      if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
      {
        boundDeviceIndex = data[ offset ] >> 4;
        if ( boundDeviceIndex == 1 )
          isMacro = true;
        boundDeviceIndex >>= 1;
        length = data[ offset++ ] & 0x0F;
      }
      else
      {
        int type = data[ offset ] >> 4; 
        if ( type == 0x80 )
          isMacro = true;
        boundDeviceIndex = data[ offset++ ] & 0x0F;
        length = data[ offset++ ];
      }  

      String text = null;
      if ( notesEnum.hasMoreElements())
        text = notesEnum.nextElement();
      if ( "".equals( text ))
        text = null;
      if ( isMacro )
      {
        Hex keyCodes = Hex.subHex( data, offset, length );
        macros.add( new Macro( keyCode, keyCodes, text ));
      }
      else 
      {
        KeyMove keyMove = null;
        Hex hex = Hex.subHex( data, offset, length );
        if ( remote.getAdvCodeFormat() == remote.HEX )
          keyMove = new KeyMove( keyCode, boundDeviceIndex, hex, text );
        else if ( remote.getEFCDigits() == 3 )
        {
          if ( length == 1 )
            keyMove = new KeyMoveKey( keyCode, boundDeviceIndex, hex, text );
          else
            keyMove = new KeyMoveEFC( keyCode, boundDeviceIndex, hex, text );
        }
        else // EFCDigits == 5
          keyMove = new KeyMoveEFC5( keyCode, boundDeviceIndex, hex, text );
        
        // check if the keymove comes from a device upgrade
        DeviceButton boundDeviceButton = remote.getDeviceButtons()[ boundDeviceIndex ];
        DeviceUpgrade boundUpgrade = findDeviceUpgrade( boundDeviceButton );
        DeviceUpgrade moveUpgrade = findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode());
        if (( boundUpgrade != null ) && ( boundUpgrade == moveUpgrade ))
        {    
          // Add the keymove to the device upgrade instead of the keymove collection
          Function f = boundUpgrade.getFunction( hex );
          if ( f == null )
          {
            if ( text == null )
              text = remote.getButtonName( keyCode );
            f = new Function( text, hex, "imported from keyMove" );
            int state = Button.NORMAL_STATE;
            Button b = remote.getButton( keyCode );
            if ( b == null )
            {
              int mask = keyCode & 0xC0;
              int baseCode = keyCode & 0x3F;
              if ( baseCode != 0 )
              {
                b = remote.getButton( baseCode );
                if (( baseCode | remote.getShiftMask()) == keyCode )
                  state = Button.SHIFTED_STATE;
                if (( baseCode | remote.getXShiftMask()) == keyCode )
                  state = Button.XSHIFTED_STATE;
              }
              else
              {
                baseCode = keyCode & ~remote.getShiftMask();
                b = remote.getButton( baseCode );
                if ( b != null )
                  state = Button.SHIFTED_STATE;
                else
                {
                  baseCode = keyCode & ~ remote.getXShiftMask();
                  b = remote.getButton( baseCode );
                  if ( b != null )
                    state = Button.XSHIFTED_STATE;
                }
              }
            }
            boundUpgrade.setFunction( b, f, state );
          }
        }
        else
          keymoves.add( keyMove );
      }
      offset += length;
    }
  }

  public int getAdvancedCodeBytesUsed()
  {
    AddressRange advCodeRange = remote.getAdvanceCodeAddress();
    int offset = advCodeRange.getStart();
    int endOffset = advCodeRange.getEnd();
    while (( offset <= endOffset ) && ( data[ offset ] != remote.getSectionTerminator()))
    {
      offset++; // skip the keyCode

      int length = 0;
      if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
        length = data[ offset++ ] & 0x0F;
      else
      {
        offset++; // skip the type
        length = data[ offset++ ];
      }  
      offset += length;
    }
    return offset - advCodeRange.getStart();
  }

  public int updateAdvancedCodes()
  {
    AddressRange range = remote.getAdvanceCodeAddress();
    int offset = range.getStart();
    for ( KeyMove keyMove : keymoves )
    {
      data[ offset++ ] = ( short )keyMove.getKeyCode();
      int lengthOffset;
      if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
      {
        int temp = keyMove.getDeviceButtonIndex() << 5;
        data[ offset ] = ( short )temp;
        lengthOffset = offset++;
      }
      else
      {
        data[ offset++ ] = ( short )( 0x10 | ( keyMove.getDeviceButtonIndex() << 4 ));
        lengthOffset = offset++;
        data[ lengthOffset ] = 0;
      }        
      Hex hex = keyMove.getData();
      int hexLength = hex.length();
      Hex.put( hex, data, offset );
      offset += hexLength;
      data[ lengthOffset ] |= ( short )hexLength;        
    }
  
    for ( Macro macro : macros )
    {
      data[ offset++ ] = ( short )macro.getKeyCode();
      int lengthOffset = 0;
      if ( remote.getAdvCodeBindFormat() == Remote.NORMAL )
      {
        data[ offset ] = 0x10;
        lengthOffset = offset++;
      }
      else
      {
        data[ offset++ ] = 0x80;
        lengthOffset = offset++;
      }        
      Hex hex = macro.getData();
      int hexLength = hex.length();
      Hex.put( hex, data, offset );
      offset += hexLength;
      data[ lengthOffset ] |= ( short )hexLength;        
    }
    data[ offset ] = 0;
    
    return offset - range.getStart();
  }
  
  public void updateCheckSums()
  {
    CheckSum[] sums = remote.getCheckSums();
    for ( int i = 0; i < sums.length; ++i )
      sums[ i ].setCheckSum( data );
  }

  private ProtocolUpgrade getProtocol( int pid )
  {
    for ( ProtocolUpgrade pu : protocols )
    {
      if ( pu.getPid() == pid )
        return pu;
    }
    return null;
  }
  
  private void decodeUpgrades( Vector< String > deviceNotes, Vector< String > protocolNotes )
  {
    AddressRange addr = remote.getUpgradeAddress();

    // first parse the protocols
    Enumeration< String > notesEnum = protocolNotes.elements();
    int offset = Hex.get( data, addr.getStart() + 2 ) - remote.getBaseAddress(); // get offset of protocol table
    int count = Hex.get( data, offset ); // get number of entries in upgrade table
    offset += 2;  // skip to first entry
    for ( int i = 0; i < count; ++i )
    {
      int pid = Hex.get( data, offset );
      int codeOffset = Hex.get( data, offset + 2 * count ) - remote.getBaseAddress();
      int nextCode = 0;
      if ( i == count - 1 ) // the last protocol, so use the start of the device table
      {
        nextCode = addr.getStart();
        nextCode = Hex.get( data, addr.getStart()) - remote.getBaseAddress();
      }
      else
        nextCode = Hex.get( data, offset + 2 * ( count + 1 )) - remote.getBaseAddress();
      Hex code = Hex.subHex( data, codeOffset, nextCode - codeOffset );
      String text = null;
      if ( notesEnum.hasMoreElements())
        text = notesEnum.nextElement();
      if ( "".equals( text ))
        text = null;
      protocols.add( new ProtocolUpgrade( pid, code, text )); 

      offset += 2; // for the next upgrade      
    }

    // now parse the devices
    notesEnum = deviceNotes.elements();
    offset = Hex.get( data, addr.getStart()) - remote.getBaseAddress(); // get offset of device table
    count = Hex.get( data, offset ); // get number of entries in upgrade table
    for ( int i = 0; i < count; ++i )
    {
      offset += 2;
      int setupCode = Hex.get( data, offset ) & 0x7FF;
      DeviceType devType = remote.getDeviceTypes()[ data[ offset ] >> 4 ];
      int codeOffset = offset + 2 * count; // compute offset to offset of upgrade code
      codeOffset = Hex.get( data, codeOffset ) - remote.getBaseAddress(); // get offset of upgrade code
      int pid = data[ codeOffset ];
      if (( data[ offset ] & 8 ) == 8 ) // pid > 0xFF
        pid += 0x100;
      
      int nextCode = 0;
      if ( i == count - 1 ) // this is the last device upgrade
      {  // try using the 1st protocol
        int pOffset = Hex.get( data, addr.getStart() + 2 ) - remote.getBaseAddress();
        int pCount = Hex.get( data, pOffset );
        pOffset += 2; // skip count & point to first PID
        if ( pCount > 0 )
          nextCode = Hex.get( data, pOffset + ( 2 * pCount )) - remote.getBaseAddress();
        else // there are no protocol upgrades, use the device upgrade table itself
          nextCode = Hex.get( data, addr.getStart()) - remote.getBaseAddress();
      }
      else
        nextCode = Hex.get( data, offset + 2 * ( count + 1 )) - remote.getBaseAddress(); // next device upgrade
      Hex deviceHex = Hex.subHex( data, codeOffset, nextCode - codeOffset );
      ProtocolUpgrade pu = getProtocol( pid );
      Hex protocolCode = null;
      if ( pu != null )
        protocolCode = pu.getCode();

      String[] aliases = remote.getDeviceTypeAliasNames();
      String alias = null;
      for ( int j = 0; j < aliases.length; ++j )
      {
        alias = aliases[ j ];
        if ( remote.getDeviceTypeByAliasName( alias ) == devType )
          break;
      }

      short[] pidHex = new short[ 2 ];
      pidHex[ 0 ] = ( short )(( pid > 0xFF ) ? 1 : 0 );
      pidHex[ 1 ] = ( short )( pid & 0xFF );
      
      String text = null;
      if ( notesEnum.hasMoreElements())
        text = notesEnum.nextElement();
      if ( "".equals( text ))
        text = null;

      DeviceUpgrade upgrade = new DeviceUpgrade();
      upgrade.importRawUpgrade( deviceHex, remote, alias, new Hex( pidHex ), protocolCode );
      upgrade.setSetupCode( setupCode );
      if ( text != null )
        upgrade.setDescription( text );
      
      devices.add( upgrade );
    }
  }

  public int getUpgradeCodeBytesUsed()
  {
    AddressRange addr = remote.getUpgradeAddress();

    int offset = Hex.get( data, addr.getStart() + 2 ) - remote.getBaseAddress(); // get offset of protocol table
    int count = Hex.get( data, offset ); // get number of protocol upgrades
    offset += 2;  // skip to first entry
    offset += ( 4 * count ); // the are 4 bytes for each entry ( 2 for PID, 2 for the code pointer ) 
    return offset - addr.getStart() - 1;
  }
  
  public int updateUpgrades()
  {
    AddressRange addr = remote.getUpgradeAddress();
    int offset = addr.getStart() + 4; // skip over the table pointers
    int devCount = devices.size();
    int prCount = protocols.size();

    // Handle the special case where there are no upgrades installed
    if (( devCount == 0 ) && ( prCount == 0 ))
    {
      Hex.put( offset + remote.getBaseAddress(), data, addr.getStart());
      Hex.put( offset + remote.getBaseAddress(), data, addr.getStart() + 2 );
      Hex.put( 0, data, offset );
      return offset - addr.getStart();
    }

    // store the device upgrades
    int[] devOffsets = new int[ devCount ];
    Enumeration< DeviceUpgrade > de = devices.elements();
    for ( int i = 0; i < devCount; ++i )
    {
      devOffsets[ i ] = offset;
      DeviceUpgrade dev = de.nextElement();
      Hex hex = dev.getUpgradeHex();
      Hex.put( hex, data, offset );
      offset += hex.length();
    }

    // store the protocol upgrades 
    int[] prOffsets = new int[ prCount ];
    Enumeration< ProtocolUpgrade > pe = protocols.elements();
    for ( int i = 0; i < prCount; ++i )
    {
      prOffsets[ i ] = offset;
      ProtocolUpgrade upgrade = pe.nextElement();
      Hex hex = upgrade.getCode();
      Hex.put( hex, data, offset );
      offset += hex.length();
    }
    
    // ser the pointer to the device table.
    Hex.put( offset + remote.getBaseAddress(), data, addr.getStart());
    
    // create the device table
    Hex.put( devCount, data, offset );
    offset += 2;
    de = devices.elements();
    for ( int i = 0; i < devCount; ++i )
    {
      DeviceUpgrade dev = de.nextElement();
      Hex.put( dev.getHexSetupCode(), data, offset );
      offset += 2;
    }
    for ( int i = 0; i < devCount; ++i )
    {
      Hex.put( devOffsets[ i ] + remote.getBaseAddress(), data, offset );
      offset+= 2;
    }
    
    // set the pointer to the protocol table
    Hex.put( offset + remote.getBaseAddress(), data, addr.getStart() + 2 );
    
    // create the protocol table
    Hex.put( prCount, data, offset );
    offset += 2;
    pe = protocols.elements();
    for ( int i = 0; i < prCount; ++i )
    {
      ProtocolUpgrade pr = pe.nextElement();
      Hex.put( pr.getPid(), data, offset );
      offset += 2;
    }
    for ( int i = 0; i < prCount; ++i )
    {
      Hex.put( prOffsets[ i ] + remote.getBaseAddress(), data, offset );
      offset+= 2;
    }
    
    return offset - addr.getStart();
  }

  public void decodeLearnedSignals( Vector< String > notes )
  {
    AddressRange addr = remote.getLearnedAddress();
    if ( addr == null )
      return;

    Enumeration< String > notesEnum = notes.elements();
    int offset = addr.getStart();
    while (( offset < addr.getEnd()) && ( data[ offset ] != remote.getSectionTerminator()))
    {
      short keyCode = data[ offset++ ];
      int device = data[ offset++ ] >> 4;
      int length = data[ offset++ ];
      String text = null;
      if ( notesEnum.hasMoreElements())
        text = notesEnum.nextElement();
      if ( "".equals( text ))
        text = null;

      learned.add( new LearnedSignal( keyCode, device, new Hex( data, offset, length ), text ));
      offset += length;
    }
  }

  public int getLearnedSignalBytesUsed()
  {
    AddressRange addr = remote.getLearnedAddress();
    if ( addr == null )
      return 0;

    int offset = addr.getStart();
    while (( offset < addr.getEnd()) && ( data[ offset ] != remote.getSectionTerminator()))
    {
      offset += 2; // skip keycode and device button
      int length = data[ offset++ ];
      offset += length;
    }
    
    return offset - addr.getStart();
  }
  
  public int updateLearnedSignals()
  {
    AddressRange addr = remote.getLearnedAddress();
    if ( addr == null )
      return 0;

    int offset = addr.getStart();
    for ( LearnedSignal l : learned )
    {
      data[ offset++ ] = ( short )l.getKeyCode();
      data[ offset++ ] = ( short )( l.getDeviceButtonIndex() << 4 );
      Hex hex = l.getData();
      data[ offset++ ] = ( short )hex.length();
      Hex.put( hex, data, offset );
      offset += hex.length();
    }
    data[ offset ] = 0;
    return offset - addr.getStart();
  }

  private void printNote( int index, String text, PrintWriter out )
    throws IOException
  {
    if (( text == null ) || ( text.length() == 0 ))
      return;
    
    out.println();
    out.print( '$' );
    out.print( toHex( index ));
    out.print( '=' );
    out.print( exportNotes( text ));
  }
  
  public void save( File file )
    throws IOException
  {
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( file )));
    for ( int i = 0; i < data.length; ++i )
    {
      if (( i % 16 ) == 0 )
      {
        if ( i != 0 )
          out.println();
        out.print( toHex( i ));
        out.print( ':' );
      }
      out.print( "  " );
      out.print( toHex( data[ i ]));
    }
    out.println();
    out.println();
    out.print( "[Notes]" );
    printNote( 0, notes, out );

    
//    int j = 0;
//    for ( Enumeration e = keymoves.elements(); e.hasMoreElements(); )
//    {
//      AdvancedCode item = ( AdvancedCode )e.nextElement();
//      printNote( 0x1000 + j, item.getNotes(), out );
//    }

//    for ( Enumeration e = macros.elements(); e.hasMoreElements(); )
//    {
//      AdvancedCode item = ( AdvancedCode )e.nextElement();
//      printNote( 0x1000 + j, item.getNotes(), out );
//    }
    
//    for ( j = 0; j < devices.size(); ++j )
//    {
//      DeviceUpgrade upgrade = ( DeviceUpgrade )devices.elementAt( j );
//      printNote( 0x3000 + j, upgrade.getNotes(), out );
//    }

//    for ( j = 0; j < protocols.size(); ++j )
//    {
//      ProtocolUpgrade protocol = ( ProtocolUpgrade )protocols.elementAt( j );
//      printNote( 0x4000 + j, protocol.getNotes(), out );
//    }

    for ( int j = 0; j < learned.size(); ++j )
    {
      LearnedSignal signal = ( LearnedSignal )learned.elementAt( j );
      printNote( 0x5000 + j, signal.getNotes(), out );
    }
    
    out.println();
    PropertyWriter pw = new PropertyWriter( out );
    
    for ( KeyMove keyMove : keymoves )
    {
      out.println();
      String className = keyMove.getClass().getName();
      int dot = className.lastIndexOf( '.' );
      className = className.substring( dot + 1 );
      out.println( className );
      keyMove.store( pw );
    }

    for ( Macro macro : macros )
    {
      out.println();
      out.println( "[Macro]" );
      macro.store( pw );
    }
    
    for ( DeviceUpgrade device : devices )
    {
      out.println();
      out.println( "[DeviceUpgrade]" );
      device.store( pw );
    }
    
    out.close();
  }

  private String importNotes( String text )
  {
    StringTokenizer st = new StringTokenizer( text, "®" );
    StringWriter sw = new StringWriter( text.length() + st.countTokens());
    PrintWriter out = new PrintWriter( sw ); 
    boolean first = true;
    while ( st.hasMoreTokens())
    {
      if ( first )
        first = false;
      else
        out.println();
      out.print( st.nextToken());
    }
    String rc = sw.getBuffer().toString();
    return rc;
  }

  private String exportNotes( String text )
    throws IOException
  {
    BufferedReader br = new BufferedReader( new StringReader( text ));
    StringBuffer buff = new StringBuffer( text.length());
    String line = br.readLine();
    while ( line != null )
    {
      buff.append( line );
      line = br.readLine();
      if ( line != null )
        buff.append( '®' );
    }
    return buff.toString();
  }

  public Remote getRemote()
  {
    return remote;
  }

  public String getNotes(){ return notes; }
  public void setNotes( String text ){ notes = text; }

  public static String toHex( int value )
  // Returns an hexadecimal string representation with 4 digits and leading 0s
  {
    return ( Integer.toHexString( 0x10000 | value ).substring( 1 ).toUpperCase());
  }

  public static String toHex( short value )
  // Returns an hexadecimal string representation with 2 digits and leading 0s
  {
    return ( Integer.toHexString( 0x100 | value ).substring( 1 ).toUpperCase());
  }

  // PropertyChangeListener
//  public void propertyChange( PropertyChangeEvent event )
//  {
//    changed = true;
//    updateAdvCodeArea();
//  }

  public short[] getData(){ return data; }
  public short[] getSavedData(){ return savedData; }
  public Vector< KeyMove > getKeyMoves(){ return keymoves; }
  public Vector< Macro > getMacros(){ return macros; }
  public Vector< DeviceUpgrade > getDeviceUpgrades(){ return devices; }
  public Vector< ProtocolUpgrade > getProtocolUpgrades(){ return protocols; }
  public Vector< LearnedSignal > getLearnedSignals(){ return learned; }

  private Remote remote = null;
  private short[] data = null;
  private short[] savedData = null;
  private Vector< KeyMove > keymoves = new Vector< KeyMove >();
  private Vector< Macro > macros = new Vector< Macro >();
  private Vector< DeviceUpgrade > devices = new Vector< DeviceUpgrade >();
  private Vector< ProtocolUpgrade > protocols = new Vector< ProtocolUpgrade >();
  private Vector< LearnedSignal > learned = new Vector< LearnedSignal >();
  private boolean changed = false;
  private String notes = null;
}
