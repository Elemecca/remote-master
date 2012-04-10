/**
 * This is essentially a translation of makehex.cpp and irp.cpp, written by John Fine.
 * Translation copyright Bengt Martensson, 2011.
 */

/* Original Copyright from the C++-version by John S. Fine: */

///////////////////////////////////////////////////////////////////////////////
// MakeHex version 4  Copyright 2004 John S. Fine
//
// You may use, copy, modify and/or distribute this program for private or
// commercial use provided that:
//
// 1)  You do not hold me responsible for any damage or negative consequences
// resulting from those activities.
//
// 2)  You include this copyright notice and disclaimer in any copy of any part
// or all of this program.
//
// I do not provide any warranty of the correctness, safety, or suitibility of
// this program for any purpose.
//
// If you do not agree to these conditions, you have no permission to use, copy,
// modify or distribute this program.
///////////////////////////////////////////////////////////////////////////////


package com.hifiremote.makehex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class consists of John F.&nbsp; Fine's C++ program makehex translated to Java.
 * 
 */
public class Makehex {

    // Following was not in original
    boolean is_valid;
    static int debug;
    static boolean debug_parser;
    static boolean debug_genHex;
    static boolean debug_parseVal;
    static boolean debug_hex;
    static boolean verbose;
    String protocolname;
    String filename;
    
    int sizeIntro;
    
    public final static String version_string = "4.0.1";
    
    // IR carrier frequency in Hz
    double m_frequency = 38400.0;
    
    // Time unit, in us
    double m_timeBase = 1.0;
    
    // Unit us
    double m_messageTime = 0.0;
    String[] m_digits = new String[16];
    String m_prefix;
    String m_suffix;
    String m_rPrefix;
    String m_rSuffix;
    boolean m_msb = false;
    HashMap<Character, String> m_def = new HashMap<Character, String>(26);
    HashMap<Character, Integer> m_value = new HashMap<Character, Integer>(26);
    String m_form;
    int[] m_device = {-1, -1, -1};
    int[] m_functions = {-1, -1, -1, -1};
    double m_cumulative;
    ArrayList<Double> m_hex = new ArrayList<Double>(100);

    int[] m_mask = new int[33];
    int m_bitGroup = 2;
    int m_pendingBits;

    final static int colon = 1;
    final static int plus = 2;
    final static int times = 3;
    final static int unary = 4;
    
    /**
     * Sets debugging parameters in the Makehex engine.
     * @param dbg debug parameter. See the code for the exact semantics.
     */
    public static void setDebug(int dbg) {
        debug = dbg;
        debug_parser = (debug & 1) != 0;
        debug_genHex = (debug & 2) != 0;
        debug_parseVal = (debug & 4) != 0;
        debug_hex = (debug & 8) != 0;
        verbose = (debug & 16) !=0;
    }
    
    /** For debugging purposes */
    @Override
    public String toString() {
        String s = "";
        s += "filename = " + filename + "\n";
        s += "frequency = " + m_frequency + "\n";
        s += "timebase = " + m_timeBase + "\n";
        s += "messagetime = " + m_messageTime + "\n";
        s += "m_rPrefix = " + this.m_rPrefix + "\n";
        s += stringify(m_digits, "m_digits") + "\n";
        s += stringify(m_functions, "m_functions") + "\n";
        s += stringify(m_device, "m_device") + "\n";
        s += "m_def: " + m_def.toString() + "\n";
        s += "m_value: " + m_value + "\n";
        s += "m_msb: " + m_msb + "\n";
        s += "m_form: " + m_form;
        return s;
    }
    
    private class Value {
        public double m_val;
        public int m_bits;
        
        @Override
        public String toString() {
            return m_val + ":" + m_bits;
        }
    };

    private static String stringify(String[] str, String name) {
        String res = "";
        for (int i = 0; i < str.length; i++)
            res += name + "[" + i + "] = " + str[i] + "; ";
        return res;
    }
    
    private static String stringify(int[] str, String name) {
        String res = "";
        for (int i = 0; i < str.length; i++)
            res += name + "[" + i + "] = " + str[i] + "; ";
        return res;
    }
    
    private void setDigit(int d, String value) {
        m_digits[d] = value;
        while (d >= m_bitGroup) {
            m_bitGroup <<= 1;
        }
    }
    
    private void getPair(int[] arr, String value) {
        String[] str = value.split("\\.");
        arr[0] = Integer.parseInt(str[0]);
        if (str.length > 1)
            arr[1] = Integer.parseInt(str[1]);
    }

    /**
     * Returns the validity of the instance.
     * 
     * @return Validity of the instance.
     */
    public boolean is_valid() {
        return is_valid;
    }
    
    boolean parseVal(Value result, StringBuffer in) {
        return parseVal(result, in, 0);
    }

    boolean parseVal(Value result, StringBuffer in, int prec /* = 0 */) {
        if (debug_parseVal)
            System.out.println("Entering parseVal in = `" + in + "', prec = " + prec);
        if (in.charAt(0) >= 'A' && in.charAt(0) <= 'Z') {
            char ndx = in.charAt(0);
            in.deleteCharAt(0);
            // Do not take the m_def value if there is a m_value (prevents defaults to be overridden).
            if (!m_value.containsKey(ndx) && m_def.containsKey(ndx) && m_def.get(ndx) != null) {
                StringBuffer in2 = new StringBuffer(m_def.get(ndx));
                //if (in2 != null && in2.length() > 0) {
                parseVal(result, in2, 0);
                if (in2.length() > 0) {
                    // error
                    System.err.println("This cannot happen");
                }
            } else {
                if (m_value.containsKey(ndx)) {
                    result.m_val = m_value.get(ndx);
                    result.m_bits = 0;
                } else {
                    System.err.println("[" + this.protocolname + "] ERROR: '" + ndx + "' is not defined.");
                    return false;
                }
            }
        } else if (in.charAt(0) >= '0' && in.charAt(0) <= '9') {
            result.m_bits = 0;
            result.m_val = 0.0;
            do {
                result.m_val = result.m_val * 10 +  Integer.parseInt(in.substring(0,1));
                in.deleteCharAt(0);
            } while (in.length() > 0 && in.charAt(0) >= '0' && in.charAt(0) <= '9');
        } else
            switch (in.charAt(0)) {
                case '-':
                    in.deleteCharAt(0);
                    parseVal(result, in, unary);
                    result.m_val = -result.m_val;
                    if (result.m_bits > 0)
                        result.m_bits = 0;
                    break;
                case '~':
                    in.deleteCharAt(0);
                    parseVal(result, in, unary);
                    result.m_val = -(result.m_val + 1);
                    if (result.m_bits > 0)
                        result.m_val = (double) (((int) result.m_val) & m_mask[result.m_bits]);
                    break;
                case '(':
                    in.deleteCharAt(0);
                    parseVal(result, in, 0);
                    if (in.charAt(0) == ')') {
                        in.deleteCharAt(0);
                    } else {
                        // error
                    }
                    break;
                default:
                    // error
                    break;
            }
        
        if (in.length() > 0 && (in.charAt(0) == 'M' || in.charAt(0) == 'm')) {
            result.m_val *= 1000;
            result.m_bits = -1;
            in.deleteCharAt(0);
        } else if (in.length() > 0 && (in.charAt(0) == 'U' || in.charAt(0) == 'u')) {
            result.m_bits = -1;
            in.deleteCharAt(0);
        }

        for (;;) {
            Value v2 = new Value();
            if (prec < times) {
                if (in.length() > 0&& in.charAt(0) == '*') {
                    in.deleteCharAt(0);
                    parseVal(v2, in, times);
                    result.m_val *= v2.m_val;
                    if (result.m_bits > 0)
                        result.m_bits = 0;
                    continue;
                }
            }
            if (in.length() > 0 && prec < plus) {
                switch (in.charAt(0)) {
                    case '+':
                        in.deleteCharAt(0);
                        parseVal(v2, in, plus);
                        result.m_val += v2.m_val;
                        if (result.m_bits > 0)
                            result.m_bits = 0;
                        continue;
                    case '-':
                        in.deleteCharAt(0);
                        parseVal(v2, in, plus);
                        result.m_val -= v2.m_val;
                        if (result.m_bits > 0)
                            result.m_bits = 0;
                        continue;
                    case '^':
                        in.deleteCharAt(0);
                        parseVal(v2, in, plus);
                        result.m_val = ((int) result.m_val) ^ ((int) v2.m_val);
                        if (result.m_bits > 0 && (v2.m_bits <= 0 || v2.m_bits > result.m_bits))
                            result.m_bits = v2.m_bits;
                        continue;
                }
            }
            if (in.length() > 0 && prec < colon) {
                if (in.charAt(0) == ':') {
                    in.deleteCharAt(0);
                    parseVal(v2, in, colon);
                    result.m_bits = (int)v2.m_val;
                if (in.length() > 0 && in.charAt(0) == ':') {
                        in.deleteCharAt(0);
                        parseVal(v2, in, colon);
                        result.m_val = (double) (((int) result.m_val) >> ((int) v2.m_val));
                    }
                    if (result.m_bits < 0) {
                        result.m_bits = -result.m_bits;
                        result.m_val = (double) (Integer.reverse((int) result.m_val) >> (32 - result.m_bits));
                    }
                    result.m_val = (double) (((int) result.m_val) & m_mask[result.m_bits]);
                    continue;
                }
            }
            break;
        }
        if (debug_parseVal)
            System.out.println(" parseVal returns: " + result + "in = " + in);
        return true;
    }
    
    int genHex(String Pattern) {
        return genHex(new StringBuffer(Pattern));
    }
    
    int genHex(StringBuffer Pattern) {
        if (debug_genHex)
            System.out.println("genHex called with `" + Pattern + "'.");
        int Result = -1;
        if (Pattern.charAt(0) == ';') {
            Result = 0;         // intro sequence is empty
            Pattern.deleteCharAt(0);
        }
        while (Pattern.length() > 0) {
            if (Pattern.charAt(0) == '*') {
                genHex((Result >= 0 && m_rPrefix != null && m_rPrefix.length() > 0) ? m_rPrefix : m_prefix);
                Pattern.deleteCharAt(0);
            } else if (Pattern.charAt(0) == '_') {
                genHex((Result >= 0 && m_rSuffix != null && !m_rSuffix.isEmpty()) ? m_rSuffix : m_suffix);
                Pattern.deleteCharAt(0);
                if (m_cumulative < m_messageTime) {
                    genHex(m_cumulative - m_messageTime);
                }
            } else if (Pattern.charAt(0) == '^') {
                Pattern.deleteCharAt(0);

                Value val = new Value();
                parseVal(val, Pattern);
                if (val.m_bits == 0)
                    val.m_val *= m_timeBase;

                if (m_cumulative < val.m_val) {
                    genHex(m_cumulative - val.m_val);
                }
            } else {
                Value val = new Value();
                parseVal(val, Pattern);

                if (val.m_bits == 0)
                    val.m_val *= m_timeBase;
                if (val.m_bits <= 0) {
                    genHex(val.m_val);
                } else {
                    int Number = (int) (val.m_val);
                    if (m_msb)
                        Number = Integer.reverse(Number) >> (32 - val.m_bits);
                    while (--val.m_bits >= 0) {
                        if (m_msb) {
                            m_pendingBits = (m_pendingBits << 1) + (Number & 1);
                            if ((m_pendingBits & m_bitGroup) != 0) {
                                genHex(m_digits[m_pendingBits - m_bitGroup]);
                                m_pendingBits = 1;
                            }
                        } else {
                            m_pendingBits = (m_pendingBits >> 1) + (Number & 1) * m_bitGroup;
                            if ((m_pendingBits & 1) != 0) {
                                genHex(m_digits[m_pendingBits >> 1]);
                                m_pendingBits = m_bitGroup;
                            }
                        }
                        Number >>= 1;
                    }
                }
            }
            if (Pattern.length() > 0 && Pattern.charAt(0) == ';') {
                if (m_cumulative < m_messageTime) {
                    genHex(m_cumulative - m_messageTime);
                }
                if ((m_hex.size() & 1) == 1) {
                    genHex(-1.0);
                }
                Result = m_hex.size();
                m_cumulative = 0.0;
            } else if (Pattern.length() > 0 && Pattern.charAt(0) != ',')
                break;
            if (Pattern.length() > 0)
                Pattern.deleteCharAt(0);
        }
        return Result;
    }
    
    void genHex(double number) {
        if (number == 0.0)
            return;
        int nHex = m_hex.size();
        if (number > 0) {
            m_cumulative += number;
            if ((nHex % 2) == 1)
                m_hex.set(nHex - 1, m_hex.get(nHex - 1) + number);
            else
                m_hex.add(number);
        } else if (nHex != 0) {
            m_cumulative -= number;
            if ((nHex % 2) == 1)
                m_hex.add(-number);
            else
                m_hex.set(nHex - 1, m_hex.get(nHex - 1) - number);
        }
    }
    
    // debugging
    private void dump_hex() {
        double sum = 0;
        for (int i = 0; i < m_hex.size(); i++ ) {
            System.out.print(m_hex.get(i) + " ");
            sum += m_hex.get(i);
        }
        System.out.println();
        System.out.println(sum);
    }

    // compute actual hex code
    private void computeHex() {
        m_hex.clear();
        m_cumulative = 0.0;
        m_pendingBits = m_msb ? 1 : m_bitGroup;
        int Single = genHex(new StringBuffer(m_form));// size of start sequence
        if (m_cumulative < m_messageTime)
            genHex(m_cumulative - m_messageTime);
        if ((m_hex.size() % 2) == 1)
            genHex(-1.0);
        if (Single < 0)
            Single = m_hex.size();
        Single >>= 1;
        sizeIntro = Single;
        if (debug_hex) {
            System.out.println(m_cumulative);
            System.out.println(m_messageTime);
            dump_hex();
        }
    }
    
    // roughly old function generate(FILE) but generates a String instead
    // of writing stuff to a file.
    private String prontoString() {
        computeHex();
        String outString = "";

        int unit;
        if (m_frequency != 0) {
            unit = (int) (4145146.0 / m_frequency + 0.5);
            outString = String.format("0000 %04X %04X %04X", unit, sizeIntro, m_hex.size() / 2 - sizeIntro);
            for (int nIndex = 0; nIndex < m_hex.size(); nIndex += 2) {
                outString += String.format(" %04X %04X", computeCycles(nIndex), computeCycles(nIndex + 1));
            }
        } else {
            double mn = m_hex.get(0);
            for (int nIndex = 1; nIndex < m_hex.size(); nIndex++) {
                if (mn > m_hex.get(nIndex))
                    mn = m_hex.get(nIndex);
            }
            unit = (int) (4.145146 * .125 * mn + 0.5);
            //unit &= -2;
            outString = String.format("0100 %04X %04X %04X", unit, sizeIntro, m_hex.size() / 2 - sizeIntro);
            for (int nIndex = 0; nIndex < m_hex.size(); nIndex += 2)
                outString += String.format(" %04X %04X", computeCycles(nIndex, mn), computeCycles(nIndex + 1, mn));
        }
        return outString;
    }
    
    private int computeCycles(int index) {
        int y = (int)(m_hex.get(index) *m_frequency/1000000.0 + 0.5);
        return y < 1 ? 1 :
               y > 0xFFFF ? 0xFFFF :
               y;
    }
    
    private int computeCycles(int index, double mn) {
        int y = (int)(m_hex.get(index)*8.0/mn + 0.5);
        return y < 1 ? 1 :
               y > 0xFFFF ? 0xFFFF :
               y;
    }
    
    /**
     * Generates a Pronto string corresponding to IR code for the selected parameter values.
     * 
     * @param device IR device
     * @param subdevice IR subdevice; use -1 for no subdevice.
     * @param function IR function, sometimes called OBC.
     * @return String in Pronto format.
     */
    public String prontoString(int device, int subdevice, int function) {
        return prontoString(device, subdevice, function, -1);
    }
    
    /**
     * Generates a Pronto string corresponding to IR code for the selected parameter values.
     * 
     * @param device IR device
     * @param subdevice IR subdevice; use -1 for no subdevice.
     * @param function IR function, sometimes called OBC.
     * @param toggle toggle value, use -1 for no (specified) toggle.
     * @return String in Pronto format.
     */
    public String prontoString(int device, int subdevice, int function, int toggle) {
        m_value.put('D', device);
        m_value.put('S', subdevice);
        m_value.put('F', function);
        if (toggle != -1)
            m_value.put('T', toggle);
        return prontoString();
    }
    
    private Makehex() {
        for (int ndx = 1; ndx < 33; ++ndx) {
            m_mask[ndx] = 2 * m_mask[ndx - 1] + 1;
        }
    }

    public Makehex(File irpfile) {
        this();
        is_valid = readIrpFile(irpfile);
    }

    private boolean eval_keyword_value(String keyword, String value) {
        if (keyword.equalsIgnoreCase("frequency"))
            m_frequency = Double.parseDouble(value);
        else if (keyword.equalsIgnoreCase("TIMEBASE"))
            m_timeBase = Double.parseDouble(value);
        else if (keyword.equalsIgnoreCase("MESSAGETIME"))
            //parseVal(val, m_next);
            //if ( val.m_bits == 0 )
            //val.m_val *= m_timeBase;
            //m_messageTime = val.m_val;
            m_messageTime = value.matches("\\d+[Mm]")
                    ? Double.parseDouble(value.substring(0, value.length()-1))*1000/*m_timeBase*/
                    : Double.parseDouble(value)*m_timeBase; 
        else if (keyword.matches("[\\d]+"))
            setDigit(Integer.parseInt(keyword), value);
        else if (keyword.equalsIgnoreCase("ZERO"))
            setDigit(0, value);
        else if (keyword.equalsIgnoreCase("ONE"))
            setDigit(1, value);
        else if (keyword.equalsIgnoreCase("TWO"))
            setDigit(2, value);
        else if (keyword.equalsIgnoreCase("THREE"))
            setDigit(3, value);
        else if (keyword.equalsIgnoreCase("PREFIX"))
            m_prefix = value;
        else if (keyword.equalsIgnoreCase("SUFFIX"))
            m_suffix = value;
        else if (keyword.equalsIgnoreCase("R-PREFIX"))
            m_rPrefix = value;
        else if (keyword.equalsIgnoreCase("R-SUFFIX"))
            m_rSuffix = value;
        else if (keyword.equalsIgnoreCase("FIRSTBIT"))
            m_msb = value.equalsIgnoreCase("msb");
        else if (keyword.equalsIgnoreCase("FORM"))
            m_form = value.toUpperCase();
        else if (keyword.regionMatches(true, 0, "DEFINE", 0, 6)
                || keyword.regionMatches(true, 0, "DEFAULT", 0, 7))
            m_def.put(keyword.charAt(keyword.length() - 1), value);
        else if (keyword.equalsIgnoreCase("DEVICE"))
            getPair(m_device, value);
        else if (keyword.equalsIgnoreCase("FUNCTION")) {
            String[] str = value.split("\\.\\.");
            //getPair(m_functions);
            m_functions[0] = Integer.parseInt(str[0]);
            if (str.length > 1)
                m_functions[2] = Integer.parseInt(str[1]);
        } else if (keyword.equalsIgnoreCase("protocol"))
            protocolname = value;
        else
            System.err.println("Unknown keyword: " + keyword);
        return true;
    }

    private boolean readIrpFile(File irpfile) {
        filename = irpfile.getName();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(irpfile)));
        } catch (FileNotFoundException ex) {
            System.err.println("File " + irpfile + " not found.");
            return false;
        }
        try {
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                line=line.replaceFirst("\\s*'.*$", "").replaceAll("\\s", "");
                //System.err.println(line);
                if (!line.startsWith("'")) {
                    String[] str = line.split("=");
                    if (str.length == 2) {
                        String keyword = str[0];
                        String value = str[1].replaceFirst("\\s*'.*$", ""); // Nuke comments
                        //System.err.println(keyword + "\t" + value);
                        eval_keyword_value(keyword, value);
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }

        // If having a true subdevice, nuke default definition
        if (m_device[1] >= 0 && m_def.containsKey('S')) {
            if (debug_parser)
                System.err.println("[" + protocolname + "] Nuking definition of 'S' since real subdevice present.");
            m_def.remove('S');
        }
        is_valid = !(
                m_form == null
                || m_form.isEmpty()
                || m_digits[0] == null
                || m_digits[1] == null
                || m_functions[0] == -1
                || m_functions[2] >= 0 && m_functions[2] != m_functions[0] && m_functions[3] != m_functions[1]
                );
        return is_valid;
    }
    
    /**
     * Process an irp file, taking parameter values (device, subdevice, functioninterval, toggle) from the irp file.
     * 
     * @param filename irp filename
     * @param out Either filename of output file, or directory, or "-" for stdout.
     * @return Success of operation.
     */
    public static boolean process_irpfile(String filename, String out) {
        return process_irpfile(filename, out, -1, -1, -1, -1, -1);
    }
    
    /**
     * Process an irp file, using given parameter values.
     * 
     * @param filename Filename of input file, extension optional, .irp default.
     * @param out Either filename of output file, or directory, or "-" for stdout.
     * @param device Devicenumber, overrides irp file.
     * @param subdevice Subdevice number, overrides irp file. Use -1 for no subdevice.
     * @param function Function number, overrides irp file.
     * @param endfunction Last function number to generate. Use -1 for no repititions.
     * @param toggle Toggle code, overrides irp file. Use -1 for no toggle, or to take code from irp file.
     * @return Success of operation.
     */
    
    public static boolean process_irpfile(String filename, String out, int device, int subdevice,
            int function, int endfunction, int toggle) {
        try {
            File f = new File(filename);
            String basename = f.getName();
            String dirname = f.getParent();
            String long_basename = basename.contains(".") ? basename : basename + ".irp";
            String filename_sans_extension = long_basename.substring(0, long_basename.lastIndexOf('.'));
            String soutFile;
            
                
            PrintStream outStream;
            File outFile = out == null || out.isEmpty() ? new File(dirname, filename_sans_extension + ".hex")
                    : (new File(out)).isDirectory() ? new File(out, filename_sans_extension + ".hex")
                    : out.equals("-") ? null
                    : new File(out);
                        
            outStream = outFile == null ? System.out : new PrintStream(outFile);
            
            if (verbose)
                System.out.println("Processing " + new File(dirname, long_basename)
                        + ", writing to " + (outFile == null ? "<stdout>" : outFile));
            
            Makehex irp = new Makehex(new File(dirname, long_basename));
            if (!irp.is_valid())
                return false;
            
            if (irp.protocolname == null)
                irp.protocolname = filename_sans_extension;
            
            if (debug_parser)
                System.out.println(irp);
            if (function == -1) {
                function = irp.m_functions[0];
                endfunction = irp.m_functions[2];
            }
            if (device == -1) {
                device = irp.m_device[0];
                if (irp.m_form.contains("S"))
                    subdevice = irp.m_device[1];
            }
            if (toggle != -1) {
                irp.m_value.put('T', toggle);
            }
            int end = endfunction == -1 ? function : endfunction;
            for (int func = function; func <= end; func++) {
                if (debug_parser)
                    System.out.println(irp);
                outStream.println("Device Code: " + device + (subdevice == -1 ? "" : "." + subdevice) + " Function: " + func);
                String prontostr = irp.prontoString(device, subdevice, func, toggle);
                if (prontostr != null)
                    outStream.println(prontostr);
                else
                    break;
            }
            outStream.close();
            return true;
            
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }

    private static void usage(int exitcode) {
        System.err.println("Usage:\n\tMakehex [options] irp-file(s)\n");
        System.err.println("where options = -D debugcode, -d deviceno, -s subdeviceno, -f functionno, -e endfunctionno, -t toggle, -o outfile");
        System.err.println("The output given to -o can be a file name, a direcory, or `-' for standard output. Default is the same filename as the iput file, with file suffix changed to `.hex'.");
        System.exit(exitcode);
    }
    
    /**
     * Utility main function for processing one or several irp files.
     * 
     * Usage:
     * makehex [options] irp-file(s)\n
     * where options = -D debugcode, -d deviceno, -s subdeviceno, -f functionno, -e endfunctionno, -t toggle, -o outfile
     * The argument given to -o can be a file name, a directory, or `-' for standard output.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //int dbg = 0;
        int device = -1;
        int subdevice = -1;
        int function = -1;
        int endfunctionno = -1;
        int toggle = -1;
        int arg_i = 0;
        String out = null;

        try {
            while (arg_i < args.length && (args[arg_i].length() > 0) && args[arg_i].charAt(0) == '-') {

                if (args[arg_i].equals("--help")) {
                    usage(0);
                }
                if (args[arg_i].equals("--version")) {
                    //System.out.println("JVM: "+ System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
                    System.out.println(version_string);
                    System.exit(0);
                }
                if (args[arg_i].equals("-D")) {
                    arg_i++;
                    setDebug(Integer.parseInt(args[arg_i++]));
                } else if (args[arg_i].equals("-d")) {
                    arg_i++;
                    device = Integer.parseInt(args[arg_i++]);
                } else if (args[arg_i].equals("-s")) {
                    arg_i++;
                    subdevice = Integer.parseInt(args[arg_i++]);
                } else if (args[arg_i].equals("-f")) {
                    arg_i++;
                    function = Integer.parseInt(args[arg_i++]);
                } else if (args[arg_i].equals("-e")) {
                    arg_i++;
                    endfunctionno = Integer.parseInt(args[arg_i++]);
                } else if (args[arg_i].equals("-t")) {
                    arg_i++;
                    toggle = Integer.parseInt(args[arg_i++]);
                } else if (args[arg_i].equals("-o")) {
                    arg_i++;
                    out = args[arg_i++];
                } else
                    usage(1);
            }
        } catch (NumberFormatException e) {
            System.err.println("NumberFormatException");
            usage(1);
        }
        
        if (arg_i == args.length)
            usage(1);

        // Using several input files and one output file (!= directory) is allowed, but senseless.

        for (int i = arg_i; i < args.length; i++) {
            process_irpfile(args[i], out, device, subdevice, function, endfunctionno, toggle);
        }
    }
}
