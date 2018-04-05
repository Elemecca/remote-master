package com.hifiremote.jp1.assembler;

public class P740data
{
  public static final String[][] AddressModes = {
    { "Nil", "B0", "" },
    { "A", "B0", "A" },
    { "Imm", "B1", "#$%02X" },
    { "Ind", "B2A1", "($%2$02X%1$02X)" },
    { "IndX", "B1Z1", "($%02X, X)" },
    { "IndY", "B1Z1", "($%02X, Y)" },
    { "Rel", "B1R1", "$%04X" },
    { "Sp", "B1M2A1", "\\$%04X" }, 
    { "Zp", "B1Z1", "$%02X" },
    { "Zp2", "B2Z2", "#$%02X, $%02X" },
    { "ZpInd", "B1Z1", "($%02X)" },
    { "ZpX", "B1Z1", "$%02X, X" },
    { "ZpY", "B1Z1", "$%02X, Y" },
    { "Abs", "B2A1", "$%2$02X%1$02X" },
    { "AbsX", "B2A1", "$%2$02X%1$02X, X" },
    { "AbsY", "B2A1", "$%2$02X%1$02X, Y" },
    { "xA0", "N1M1", "%d, A" },
    { "xA1", "N1B1R1M1", "%d, A, $%04X" },
    { "xZp1", "N1B1Z1M1", "%d, $%02X" }, 
    { "xZp2", "N1B2R2Z1M1", "%d, $%02X, $%04X" },
    { "EQU4", "", "$%04X" },
    { "EQU2", "", "$%02X" }
  };

  public static final String[][] Instructions = {
    { "BRK", "Nil" },        { "ORA", "IndX" },
    { "JSR", "ZpInd" },      { "BBS", "xA1" },
    { "*", "Nil" },          { "ORA", "Zp" },
    { "ASL", "Zp" },         { "BBS", "xZp2" },
    { "PHP", "Nil" },        { "ORA", "Imm" },
    { "ASL", "A" },          { "SEB", "xA0" },
    { "*", "Nil" },          { "ORA", "Abs" },
    { "ASL", "Abs" },        { "SEB", "xZp1" },

    { "BPL", "Rel" },        { "ORA", "IndY" },
    { "CLT", "Nil" },        { "BBC", "xA1" },
    { "*", "Nil" },          { "ORA", "ZpX" },
    { "ASL", "ZpX" },        { "BBC", "xZp2" },
    { "CLC", "Nil" },        { "ORA", "AbsY" },
    { "DEC", "A" },          { "CLB", "xA0" },
    { "*", "Nil" },          { "ORA", "AbsX" },
    { "ASL", "AbsX" },       { "CLB", "xZp1" },

    { "JSR", "Abs" },        { "AND", "IndX" },
    { "JSR", "Sp" },         { "BBS", "xA1" },
    { "BIT", "Zp" },         { "AND", "Zp" },
    { "ROL", "Zp" },         { "BBS", "xZp2" },
    { "PLP", "Nil" },        { "AND", "Imm" },
    { "ROL", "A" },          { "SEB", "xA0" },
    { "BIT", "Abs" },        { "AND", "Abs" },
    { "ROL", "Abs" },        { "SEB", "xZp1" },

    { "BMI", "Rel" },        { "AND", "IndY" },
    { "SET", "Nil" },        { "BBC", "xA1" },
    { "*", "Nil" },          { "AND", "ZpX" },
    { "ROL", "ZpX" },        { "BBC", "xZp2" },
    { "SEC", "Nil" },        { "AND", "AbsY" },
    { "INC", "A" },          { "CLB", "xA0" },
    { "LDM", "Zp2" },        { "AND", "AbsX" },
    { "ROL", "AbsX" },       { "CLB", "xZp1" },

    { "RTI", "Nil" },        { "EOR", "IndX" },
    { "STP", "Nil" },        { "BBS", "xA1" },
    { "COM", "Zp" },         { "EOR", "Zp" },
    { "LSR", "Zp" },         { "BBS", "xZp2" },
    { "PHA", "Nil" },        { "EOR", "Imm" },
    { "LSR", "A" },          { "SEB", "xA0" },
    { "JMP", "Abs" },        { "EOR", "Abs" },
    { "LSR", "Abs" },        { "SEB", "xZp1" },

    { "BVC", "Rel" },        { "EOR", "IndY" },
    { "*", "Nil" },          { "BBC", "xA1" },
    { "*", "Nil" },          { "EOR", "ZpX" },
    { "LSR", "ZpX" },        { "BBC", "xZp2" },
    { "CLI", "Nil" },        { "EOR", "AbsY" },
    { "*", "Nil" },          { "CLB", "xA0" },
    { "*", "Nil" },          { "EOR", "AbsX" },
    { "LSR", "AbsX" },       { "CLB", "xZp1" },

    { "RTS", "Nil" },        { "ADC", "IndX" },
    { "MUL", "ZpX" },        { "BBS", "xA1" },
    { "TST", "Zp" },         { "ADC", "Zp" },
    { "ROR", "Zp" },         { "BBS", "xZp2" },
    { "PLA", "Nil" },        { "ADC", "Imm" },
    { "ROR", "A" },          { "SEB", "xA0" },
    { "JMP", "Ind" },        { "ADC", "Abs" },
    { "ROR", "Abs" },        { "SEB", "xZp1" },

    { "BVS", "Rel" },        { "ADC", "IndY" },
    { "*", "Nil" },          { "BBC", "xA1" },
    { "*", "Nil" },          { "ADC", "ZpX" },
    { "ROR", "ZpX" },        { "BBC", "xZp2" },
    { "SEI", "Nil" },        { "ADC", "AbsY" },
    { "*", "Nil" },          { "CLB", "xA0" },
    { "*", "Nil" },          { "ADC", "AbsX" },
    { "ROR", "AbsX" },       { "CLB", "xZp1" },

    { "BRA", "Rel" },        { "STA", "IndX" },
    { "RRF", "Zp" },         { "BBS", "xA1" },
    { "STY", "Zp" },         { "STA", "Zp" },
    { "STX", "Zp" },         { "BBS", "xZp2" },
    { "DEY", "Nil" },        { "*", "Nil" },
    { "TXA", "Nil" },        { "SEB", "xA0" },
    { "STY", "Abs" },        { "STA", "Abs" },
    { "STX", "Abs" },        { "SEB", "xZp1" },

    { "BCC", "Rel" },        { "STA", "IndY" },
    { "*", "Nil" },          { "BBC", "xA1" },
    { "STY", "ZpX" },        { "STA", "ZpX" },
    { "STX", "ZpY" },        { "BBC", "xZp2" },
    { "TYA", "Nil" },        { "STA", "AbsY" },
    { "TXS", "Nil" },        { "CLB", "xA0" },
    { "*", "Nil" },          { "STA", "AbsX" },
    { "*", "Nil" },          { "CLB", "xZp1" },

    { "LDY", "Imm" },        { "LDA", "IndX" },
    { "LDX", "Imm" },        { "BBS", "xA1" },
    { "LDY", "Zp" },         { "LDA", "Zp" },
    { "LDX", "Zp" },         { "BBS", "xZp2" },
    { "TAY", "Nil" },        { "LDA", "Imm" },
    { "TAX", "Nil" },        { "SEB", "xA0" },
    { "LDY", "Abs" },        { "LDA", "Abs" },
    { "LDX", "Abs" },        { "SEB", "xZp1" },

    { "BCS", "Rel" },        { "LDA", "IndY" },
    { "JMP", "ZpInd" },      { "BBC", "xA1" },
    { "LDY", "ZpX" },        { "LDA", "ZpX" },
    { "LDX", "ZpY" },        { "BBC", "xZp2" },
    { "CLV", "Nil" },        { "LDA", "AbsY" },
    { "TSX", "Nil" },        { "CLB", "xA0" },
    { "LDY", "AbsX" },       { "LDA", "AbsX" },
    { "LDX", "AbsY" },       { "CLB", "xZp1" },

    { "CPY", "Imm" },        { "CMP", "IndX" },
    { "WIT", "Nil" },        { "BBS", "xA1" },
    { "CPY", "Zp" },         { "CMP", "Zp" },
    { "DEC", "Zp" },         { "BBS", "xZp2" },
    { "INY", "Nil" },        { "CMP", "Imm" },
    { "DEX", "Nil" },        { "SEB", "xA0" },
    { "CPY", "Abs" },        { "CMP", "Abs" },
    { "DEC", "Abs" },        { "SEB", "xZp1" },

    { "BNE", "Rel" },        { "CMP", "IndY" },
    { "*", "Nil" },          { "BBC", "xA1" },
    { "*", "Nil" },          { "CMP", "ZpX" },
    { "DEC", "ZpX" },        { "BBC", "xZp2" },
    { "CLD", "Nil" },        { "CMP", "AbsY" },
    { "*", "Nil" },          { "CLB", "xA0" },
    { "*", "Nil" },          { "CMP", "AbsX" },
    { "DEC", "AbsX" },       { "CLB", "xZp1" },

    { "CPX", "Imm" },        { "SBC", "IndX" },
    { "DIV", "ZpX" },        { "BBS", "xA1" },
    { "CPX", "Zp" },         { "SBC", "Zp" },
    { "INC", "Zp" },         { "BBS", "xZp2" },
    { "INX", "Nil" },        { "SBC", "Imm" },
    { "NOP", "Nil" },        { "SEB", "xA0" },
    { "CPX", "Abs" },        { "SBC", "Abs" },
    { "INC", "Abs" },        { "SEB", "xZp1" },

    { "BEQ", "Rel" },        { "SBC", "IndY" },
    { "*", "Nil" },          { "BBC", "xA1" },
    { "*", "Nil" },          { "SBC", "ZpX" },
    { "INC", "ZpX" },        { "BBC", "xZp2" },
    { "SED", "Nil" },        { "SBC", "AbsY" },
    { "*", "Nil" },          { "CLB", "xA0" },
    { "*", "Nil" },          { "SBC", "AbsX" },
    { "INC", "AbsX" },       { "CLB", "xZp1" }
  };
  
  public static final String[][] absLabels = {
    { "XmitIR", "FF00" },
    { "TestRptReqd", "FF06" }
  };
  
  public static final String[][] zeroLabels = {
    { "DCBUF", "5D", "DCBUF+", "0A" }, 
    { "PF0", "7E", "PF", "05" },
    { "PD00", "6A", "PD", "14" },
    { "DBYTES", "69" },
    { "FLAGS", "5A" },
    { "RPT", "83" }
  };
  
  public static final int[] oscData = { 2000000, 16, 5 };


}
