package com.hifiremote.jp1;

//import java.util.LinkedHashMap;

//import com.hifiremote.jp1.AssemblerOpCode.AddressMode;
//import com.hifiremote.jp1.assembler.MAXQ610data;

public class MAXQProcessor extends LittleEndianProcessor
{
  public MAXQProcessor( String name )
  {
    super( name );
    // True RAM Address not yet known, but the 2k of data memory in word mode is
    // addressed from $0000 to $03FF.  For now, try $0100
    setRAMAddress( 0x0100 );
  }
  
  @Override
  public String getEquivalentName()
  {
    return "MAXQ610";
  }
  
//  @Override
//  public AssemblerOpCode getOpCode( Hex hex )
//  {
//    if ( hex == null || hex.length() == 0 ) return null;
//    AssemblerOpCode opCode = new AssemblerOpCode();
//    LinkedHashMap< String, AddressMode > modes = getAddressModes();
//    opCode.setHex( hex.subHex( 0, 2 ) );
//    opCode.setLength( 2 );
//    short[] data = opCode.getHex().getData();
//    int flag = data[ 0 ] >> 7;
//    int dIndex = ( data[ 0 ] >> 4 ) & 7;
//    int dMod = data[ 0 ] & 0x0F;
//    int sIndex = data[ 1 ] >> 4;
//    int sMod = data[ 1 ] & 0x0F;
//    if ( dMod == 10 )
//    {
//      if ( flag == 0 )
//      {
//        if ( dIndex > 0 )
//        {
//          opCode.setName( MAXQ610data.aluOps[ dIndex ] );
//          opCode.setMode( modes.get( "Imm" ) );
//        }
//      }
//      else  // flag == 1
//      {
//        if ( dIndex == 0 )
//        {
//          if ( sMod == 10 )
//          {
//            opCode.setName( MAXQ610data.accOps[ sIndex ] );
//            opCode.setMode( modes.get( "Nil" ) );
//          }
//        }
//        else if ( sMod != 10 ) // dIndex > 0 
//        {
//          opCode.setName( MAXQ610data.aluOps[ dIndex ] );
//          opCode.setMode( modes.get( "Src" ) );
//        }
//        else if ( dIndex < 4 ) // sMod == 10, dIndex > 0
//        {
//          opCode.setName( MAXQ610data.aluOps[ dIndex ] );
//          opCode.setMode( modes.get( "Accb" ) );
//        }
//        else if ( dIndex == 5 && sIndex < 4 ) // sMod == 10
//        {
//          opCode.setName( MAXQ610data.miscOpsC[ sIndex ] );
//          opCode.setMode( modes.get( MAXQ610data.miscModesC[ sIndex ] ) );
//        }
//        else if ( dIndex == 6 ) // sMod == 10
//        {
//          opCode.setName( "MOVE" );
//          opCode.setMode( modes.get( "CAccb" ) );
//        }
//        else if ( dIndex == 7 ) // sMod == 10
//        {
//          opCode.setName( "MOVE" );
//          opCode.setMode( modes.get( "AccbC" ) );
//        }
//      }
//    }
//    else if ( dMod == 12 )
//    {
//      if ( flag == 0 )
//      {
//        opCode.setName( "JUMP" );
//        opCode.setMode( modes.get( "CondImm" ) );
//      }
//      else  // flag == 1
//      {
//        if ( sMod == 13 )
//        {
//          if ( ( sIndex & 7 ) == 0 && ( dIndex & 3 ) != 3 )
//          {
//            opCode.setName( sIndex == 0 ? "RET" : "RETI" );
//            opCode.setMode( modes.get( "Cond" ) );
//          }
//        }
//        else if ( ( dIndex & 3 ) != 3 )
//        {
//          opCode.setName( "JUMP" );
//          opCode.setMode( modes.get( "CondSrc" ) );
//        }
//        
//      }
//    }
//    else if ( dMod == 13 )
//    {
//      if ( flag == 0 )
//      {
//        String name = MAXQ610data.miscOpsP[ dIndex ];
//        opCode.setName( name );
//        if ( !name.equals( "*" ) )
//        {
//          opCode.setMode( modes.get( MAXQ610data.miscModesPImm[ dIndex ] ) );
//        }
//      }
//      else  // flag == 1
//      {
//        String name = MAXQ610data.miscOpsP[ dIndex ];
//        opCode.setName( name );
//        if ( !name.equals( "*" ) )
//        {
//          opCode.setMode( modes.get( MAXQ610data.miscModesP[ dIndex ] ) );
//        }
//      }
//    }
//    else if ( dMod == 8 && dIndex == 7 )
//    {
//      opCode.setName( "CMP" );
//      opCode.setMode( modes.get( flag == 0 ? "Imm" : "Src" ) );
//    }
//    else if ( dMod == 7 )
//    {
//      opCode.setName( "MOVE" );
//      opCode.setMode( modes.get( flag == 0 ? "CImmb" : "CSrcb" ) );
//    }
//    
//    if ( opCode.getName().equals( "*" ) )
//    {
//      if ( sMod == 7 && flag == 1 )
//      {
//        opCode.setName( "MOVE" );
//        opCode.setMode( modes.get( sIndex < 8 ? "DstbImm0" : "DstbImm1" ) );
//      }
//      else if ( sMod == 13 && ( sIndex & 7 ) == 0 && flag == 1 )
//      {
//        opCode.setName( sIndex == 0 ? "POP" : "POPI" );
//        opCode.setMode( modes.get( "Dst" ) );
//      }
//      else
//      {
//        opCode.setName( "MOVE" );
//        opCode.setMode( modes.get( flag == 0 ? "DstImm" : "DstSrc" ) );
//      }
//    }
//
//    return opCode;
//  }
//  
//  @Override
//  public String getConditionCode( int n )
//  {
//    return MAXQ610data.conditionCodes[ n ];
//  }
}
