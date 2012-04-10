/*
	Copyright (C) 2010 Graham Dixon, 2011 Bengt Martensson

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.hifiremote.exchangeir;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.Pronto;

/**
 * This class is a Java translation of the Analyze function of Graham Dixon's ("mathdon") C++ library ExchangeIR.
 */
public class Analyzer {
    
    private static final String debugFilename = "debug.txt";
    private PrintStream debugFile = null;
    
    private class Burst {

        /* Burst times, in microseconds, on/off */
        int[] times = new int[2];
        
        /** Total time in this burst */
        double[] totals = new double[2];
        
        /** Duration in units, integer part */
        int[] units = new int[2];
        
        /** Duration in units, fractional part times 100 */
        short[] fracs = new short[2];
        
        /** Number of references */
        short count;
        
        /** Number of references in intro, repetition, and ending sections respectively */
        short[] sect_counts = new short[3];
        
        int[] cumulative = new int[3];
        short type = -1;

        public Burst() {
        }

        public Burst(int iOn, int iOff) {
            this();
            times[0] = Math.abs(iOn);
            times[1] = Math.abs(iOff);
        }
        
        public void dump() {
            if (debug) {
                dumpIntArray(times);
                dumpDoubleArray(totals);
                dumpIntArray(units);
                dumpShortArray(fracs);
                debugFile.print(count + "\t");
                dumpShortArray(sect_counts);
                dumpIntArray(cumulative);
                debugFile.println(type);
            }
        }
    }
    
    private void dumpBursts() {
        if (debug) {
            for (int i = 0; i < m_Bursts.size(); i++)
                m_Bursts.get(i).dump();
            debugFile.println();
        }
    }

    int m_errlimit;
    int m_unit;
    int m_unit2;
    int[] m_databits = new int[3];
    int[] m_biphase = new int[3];
    int[] m_RC6 = new int[3];
    int m_type;		// Format type
    final static int DATASIZE = 40;
    int[] m_data = new int[DATASIZE];
    int[] m_datacounts = new int[26];
    int m_dataNdx;
    String m_altLeadout;

     /** number basis for outputs */
    // 0 means: use 16, but with $ instead of 0x as prefix.
    int basis;
    
    private ArrayList<Burst> m_Bursts = new ArrayList<Burst>();
    
    private StringBuffer irp = new StringBuffer();
    private int[] signal_as_burst_indices = null;
    
    private boolean debug = false;
    private static int default_errlimit = 10;
    
    public final static String versionString = "0.0.8";
    
    private void dump() {
        if (!debug)
            return;
        
        debugFile.println("");
        debugFile.println(m_unit + "\t"
                + m_unit2 + "\t"
                + m_type + "\t"
                + m_dataNdx + "\t" + m_altLeadout);
        dumpIntArray(m_databits);
        dumpIntArray(m_biphase);
        dumpIntArray(m_RC6);
        debugFile.println("");
        dumpIntArray(m_data);
        debugFile.println("");
        dumpIntArray(m_datacounts);
        debugFile.println();
        debugFile.println(m_datacounts[25]);
    }
    
    private void dumpIntArray(int[] array) {
        for(int i : array)
            debugFile.print(i + "\t");
    }
    
    private void dumpShortArray(short[] array) {
        for(short i : array)
            debugFile.print(i + "\t");
    }
    
    private void dumpDoubleArray(double[] array) {
        for(double x : array)
            debugFile.print(x + "\t");
    }

   /**
     * Removes last character of the argument, in place.
     * @param sb StringBuffer to be chop-ped.
     */
    private static void chop(StringBuffer sb) {
        sb.setLength(sb.length() - 1);
    }
    
    private static int log2(int x) {
        return (int)(Math.log(x)/Math.log(2)+0.0001);
    }
    
    private static double modf(double x) {
        return x - (long) x;
    }
    
    private static String basisPrefix(int basis) {
        return basis == 2 ? "0b"
                : basis == 4 ? "0q"
                : basis == 8 ? "0" 
                : basis == 16 ? "0x"
                : "$";
    }
    
    private void OutputDebugString(String s) {
        if (debug)
            System.out.println(s);
    }
    
    /**
     * Construct an Analyzer from the input arguments.
     * @param irSignal
     */
    public Analyzer(IrSignal irSignal) {
        this(irSignal, default_errlimit, 0, false);
    }
    
    /**
     * Construct an Analyzer.
     * @param irSignal
     * @param debug Turn on debugging, boolean
     */
    public Analyzer(IrSignal irSignal, boolean debug) {
        this(irSignal, default_errlimit, 0, debug);
    }
    
    
    // TODO: move base parameter from constructor to getIrp().
    /**
     * Construct an Analyzer from the input arguments.
     * 
     * @param irSignal
     * @param errlimit 
     * @param basis
     * @param debug 
     */
    public Analyzer(IrSignal irSignal, int errlimit, int basis, boolean debug) {
        this(irSignal.toIntArray(), irSignal.getIntroLength()/2, irSignal.getRepeatLength()/2,
                irSignal.getEndingLength()/2, 1, (int)irSignal.getFrequency(), errlimit, basis, debug);
    }
    
    /**
     * Input is a signal as a timing list of alternating MARK and SPACE times in 
     * microseconds, together with the burst counts of the single, repeat and extra sections and the 
     * number of copies of the repeat section in the timing list.  The length (in bursts)
     * of the timing list is:
     *		sngl_count + rpts*rpt_count + extra_count
     * These counts may, but need not, have been obtained from the timing list by the FindRepeat function.
     *
     * This function identifies the distinct bursts in the signal and then averages the times in 
     * bursts so taken to be nominally the same. The 'errlimit' parameter is used in determining whether
     * two times should be considered as nominally equal.  Two times are nominally equal if they are either
     * within 2.5% of one another or if their difference in microseconds does not exceed 'errlimit'.
     * The 'errlimit' criterion represents a variation inherent in the signal capture mechanism, the
     * percentage criterion represents natural variations in the signal.
     * 
     * @param times Timing list of alternating MARK and SPACE times in microseconds, together with the burst counts of the single, repeat and extra sections and the number of copies of the repeat section in the timing list.
     * @param sngl_count Number of burst pairs in intro sequence.
     * @param rpt_count Number of burst pairs in repeat sequence.
     * @param extra_count Number of burst pairs in ending sequence.
     * @param rpts Number of actual repetitions contained in the times array.
     * @param errlimit Is used in determining whether two times should be considered as nominally equal.
     * @param freq Is in Hz and used only to provide the frequency value in the IRP. It should be >= 0, but -1 may be used to signify that the frequency is not needed.
     * @param basis The number base to be used for the data output in the IRP form.
     * @param debug If true, produce debug messages.
     */
    public Analyzer(int[] times, int sngl_count, int rpt_count, int extra_count,
            int rpts, int freq, int errlimit, int basis, boolean debug) {
        if (debug)
            try {
                debugFile = new PrintStream(debugFilename);
            } catch (FileNotFoundException ex) {
            }
        this.debug = debug;
        this.basis = basis;
        int burst_ndx = 0;
        int i = 0;
        int ii = 0;
        int j = 0;
        int iEnd = sngl_count + rpts * rpt_count + extra_count;
        int rpt_offset = 0;
        String dbg_str;
        m_errlimit = errlimit;
        m_Bursts.clear();
        
        int[] signal_out = new int[2*iEnd];

        // Find burst pairs, and compute statistics about their usage.
        for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
            boolean found = false;
            if (i == sngl_count + rpt_count) {
                ii = sngl_count + rpts * rpt_count;
            }
            for (j = 0; j < burst_ndx; j++) {
                if (equalTimes(m_Bursts.get(j).times, 0, times, 2 * ii, 2)) {
                    signal_out[2 * i] = j;
                    signal_out[2 * i + 1] = 0;
                    found = true;
                    if (i < sngl_count)
                        m_Bursts.get(j).sect_counts[0]++;
                    else if (i < sngl_count + rpt_count)
                        m_Bursts.get(j).sect_counts[1]++;
                    else
                        m_Bursts.get(j).sect_counts[2]++;
                    break;
                }
            }
            if (!found) {
                m_Bursts.add(burst_ndx, new Burst(times[2 * ii], times[2 * ii + 1]));
                if (i < sngl_count)
                    m_Bursts.get(burst_ndx).sect_counts[0]++;
                else if (i < sngl_count + rpt_count)
                    m_Bursts.get(burst_ndx).sect_counts[1]++;
                else
                    m_Bursts.get(burst_ndx).sect_counts[2]++;
                signal_out[2 * i] = burst_ndx++;
                signal_out[2 * i + 1] = 0;
            }
            ii++;
        }
        if (debug) {
            dumpBursts();    
            for (int i1 = 0; i1 < iEnd; i1++)
                debugFile.println(signal_out[i1]);
        
            debugFile.println();
        }

        ii = 0;
        for (i = 0; i < iEnd; i++) {
            if (i > sngl_count && i < sngl_count + rpts * rpt_count && rpt_count > 0
                    && (i - sngl_count) % rpt_count == 0)
                ii = sngl_count;
            m_Bursts.get(signal_out[2 * ii]).totals[0] += Math.abs(times[2 * i]);
            if (rpt_count == 0 || rpts < 2 || extra_count > 0 || i < iEnd - 1) {
                // Only include the final lead-out if there are extra bursts or no repeats
                m_Bursts.get(signal_out[2 * ii]).totals[1] += Math.abs(times[2 * i + 1]);
            }
            m_Bursts.get(signal_out[2 * ii]).count++;
            ii++;
        }
        dumpBursts();
  
        m_unit = 99999;
        int min_off = 99999;

        for (i = 0; i < m_Bursts.size(); i++) {
            if (m_Bursts.get(i).count > 0) {
                int n = m_Bursts.get(i).count;
                m_Bursts.get(i).times[0] = (int) (m_Bursts.get(i).totals[0] / n + 0.5);
                // If there are repeats but no extra bursts then final lead-out has not been included
                if (rpt_count > 0 && rpts > 1 && extra_count == 0 && i == signal_out[2 * (sngl_count + rpt_count - 1)])
                    n--;
                if (n > 0)
                    m_Bursts.get(i).times[1] = (int) (m_Bursts.get(i).totals[1] / n + 0.5);
                m_unit = Math.min(m_unit, m_Bursts.get(i).times[0]);
                min_off = Math.min(min_off, m_Bursts.get(i).times[1]);
            }
        }
        
        dumpBursts();
        m_altLeadout = "";

        for (i = 0; i < m_Bursts.size(); i++) {
            short[] c = m_Bursts.get(i).sect_counts;
            int li_ndx = (rpt_count > 0) ? signal_out[2 * sngl_count] : 0;
            
            // Mark as lead-out (type 101) in either of the following circumstances:
            // (1) it is last burst of a single or extra section and occurs no-where else, or
            // (2) it is the last burst of a repeat section and the burst duration exceeds
            // 75% of the duration of the first burst of that section.  This second condition
            // is to handle cases where frame division is by lead-in rather than lead-out.
            // If the last burst is a data burst and first burst is a dividing lead-in it will
            // fail this test.  Note that although all protocols have a lead-out, this situation
            // can arise (eg X10 protocol) when the lead-out burst has a long MARK rather than
            // a long SPACE, so is not identified as a lead-out at this stage.
            
            if (rpt_count > 0 && i == signal_out[2 * (sngl_count + rpt_count - 1)]
                    && m_Bursts.get(i).times[0] + m_Bursts.get(i).times[1]
                    > 0.75 * (m_Bursts.get(li_ndx).times[0] + m_Bursts.get(li_ndx).times[1]))
                m_Bursts.get(i).type = 101;
            if (i == signal_out[2 * (sngl_count + rpt_count + extra_count - 1)]
                    && c[0] == 0 && c[1] == 0 && c[2] == 1)
                m_Bursts.get(i).type = 101;
            //if (i == signal_out[2 * (sngl_count - 1)] && c[0] == 1 && c[1] == 0 && c[2] == 0)
            if (sngl_count > 0 && i == signal_out[2 * (sngl_count - 1)] && c[0] == 1 && c[1] == 0 && c[2] == 0)
                m_Bursts.get(i).type = 101;
            
            // Mark SPACEs of more than 10 times min SPACE as special (type 102) if they occur at most
            // once in any one section and have MARK consistent with main lead-out.  These are candidates
            // for being a cumulative-style lead-out of a main frame that has ditto repeats with a lead-out
            // of same cumulative value but larger Math.absolute value.
            
            if (m_Bursts.get(i).times[1] > 10 * min_off && m_Bursts.get(i).type == -1
                    && m_Bursts.get(i).times[0] > 0.9 * m_Bursts.get(signal_out[2 * (sngl_count + rpt_count - 1)]).times[0]
                    && m_Bursts.get(i).times[0] < 1.1 * m_Bursts.get(signal_out[2 * (sngl_count + rpt_count - 1)]).times[0]
                    && c[0] < 2 && c[1] < 2 && c[2] < 2 && c[0] + c[1] + c[2] > 0)
                m_Bursts.get(i).type = 102;
        }

        // Clear cumulative counts
        for (i = 0; i < m_Bursts.size(); i++) {
            m_Bursts.get(i).cumulative[0] = m_Bursts.get(i).cumulative[1] = m_Bursts.get(i).cumulative[2] = 0;
        }
        dumpBursts();

        // Calculate cumulative times in each section to each lead-out (101) or special (102) burst.
        int cumul = 0, sect = 0;
        Burst b;
        for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
            if (i == sngl_count) {
                cumul = 0;
                sect = 1;
            } else if (i == sngl_count + rpt_count) {
                cumul = 0;
                sect = 2;
            }
            b = m_Bursts.get(signal_out[2 * i]);
            cumul += b.times[0] + b.times[1];
            if (b.type > 100 && b.type < 103) {
                b.cumulative[sect] = cumul;
                cumul = 0;
            }
        }

        int iMainLead = signal_out[2 * (sngl_count + rpt_count - 1)];
        int consistent = 0;	// Count of number of bursts with cumulative times consistent with main leadout.
        cumul = m_Bursts.get(iMainLead).cumulative[1];	// Main leadout cumulative time
        if (cumul > 0)
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type > 100 && i != iMainLead)
                    // Test consistency of cumulative totals (exclude extra section)
                    for (int j1 = 0; j1 < 2; j1++) {
                        if (m_Bursts.get(i).cumulative[j1] > 0.95 * cumul && m_Bursts.get(i).cumulative[j1] < 1.05 * cumul)
                            consistent += 1;
                    }
            }

        // If there are any, mark as type 103 those 101's and 102's with consistent cumulative times.
        if (consistent > 0) {
            for (i = 0; i < m_Bursts.size(); i++) {
                for (int j2 = 0; j2 < 3; j2++) {
                    if (m_Bursts.get(i).cumulative[j2] > 0.95 * cumul && m_Bursts.get(i).cumulative[j2] < 1.05 * cumul)
                        m_Bursts.get(i).type = 103;
                }
            }
        } else if (cumul > 0 && !(sngl_count >= 4 && rpt_count >= 4)) {
            // In other cases there appears to be real single and repeat sections with
            // different cumulative leadouts, so presumably they are genuinely not cumulative.
            //FIXME sprintf(m_altLeadout, "; Alt leadout form: ^%dm", (cumul+500)/1000);
            m_altLeadout = String.format("^%dm", (cumul+500)/1000);
        }

	// See if there are any remaining 102s.  If so, don't show alt leadout as value may be wrong.
        for (i = 0; i < m_Bursts.size(); i++) {
            if (m_Bursts.get(i).type == 102)
                m_altLeadout = "";
        }

        if (m_Bursts.size() > 3) {
            // There should be at least two different data bursts and a lead-out burst, so if fewer than 4
            // different bursts, don't seek lead-in.
            seekLeadin(sngl_count, rpt_count, extra_count, signal_out, 0, 100);
        }
        dumpBursts();
        m_type = 0;

	// rpt_offset is set nonzero by identify(...) if the identification analysis determines that the
	// start of the repeat section has been misidentified, eg as a lead-in that should really be
	// the lead-out of a repeat section offset by one burst.
	rpt_offset = identify(sngl_count, rpt_count, extra_count, signal_out, freq, irp);

	// m_type 99 is returned when identify(...) has marked another special type of burst (type 105) and
	// the identification needs to be re-done.
	if (m_type == 99)
            rpt_offset = identify(sngl_count, rpt_count, extra_count, signal_out, freq, irp);
	if (m_type == 99)
            m_type = 0;	// Safeguard, should not happen.

	// If no identification so far, seek a double lead-in (type 104) and try again.
	if (m_type == 0 && m_Bursts.size() > 3 && seekLeadin(sngl_count, rpt_count, extra_count, signal_out, 1, 104))
            rpt_offset = identify(sngl_count, rpt_count, extra_count, signal_out, freq, irp);
 
        
        signal_as_burst_indices = new int[sngl_count + rpt_count + extra_count];
        for (i = 0; i < sngl_count + rpt_count + extra_count; i++)
                signal_as_burst_indices[i] = signal_out[2 * i];
    
        dumpBursts();
        dump();
        //return (freq > -2) ? m_Bursts.size() : rpt_offset;
    }
    
    /**
     * Returns the signal as a sequence of indices in the burst pairs, as can be returned from getBursts().
     * @return integer array of indices into the burst pairs.
     */
    public int[] getSignalAsBurstIndices() {
        return signal_as_burst_indices.clone();
    }
    
    /**
     * Returns signal as array of durations.
     * @return integer array of microsecond durations
     */
    public int[] getSignal() {
        int[] signal = new int[2 * signal_as_burst_indices.length];
        int ndx;
        for (int i = 0; i < signal_as_burst_indices.length; i++) {
            ndx = signal_as_burst_indices[i];
            signal[2 * i] = m_Bursts.get(ndx).times[0];
            signal[2 * i + 1] = m_Bursts.get(ndx).times[1];
        }
        return signal;
    }
    
    /**
     * Returns the computed burst pairs.
     * @return Burst pairs
     */
    
    public int[][] getBursts() {
        int[][] bursts = new int[m_Bursts.size()][2];
        for (int i = 0; i < m_Bursts.size(); i++) {
            bursts[i][0] = m_Bursts.get(i).times[0];
            bursts[i][1] = m_Bursts.get(i).times[1];
        }
        return bursts;
    }
    
    /**
     * Returns computed IRP string.
     * @return IRP String
     */
    public String getIrp() {
        return new String(irp);
    }
    
    /**
     * Returns alternative leadout sequence, or null if empty.
     * @return the alt leadout string
     */
    public String getAltLeadout() {
        return m_altLeadout.isEmpty() ? null : m_altLeadout;
    }
    
    /**
     * Returns computed IRP string, together with alterative leadout string. The result may not be parseable!
     * @return IRP string, possibly with alternative leadout added.
     */
    public String getIrpWithAltLeadout() {
        return getIrp() + (m_altLeadout.isEmpty() ? "" : ("; Alt leadout form: " + m_altLeadout)); 
    }
    
   /**
     * Returns version string.
     * @return Version number as string.
     */
    public static String getVersion() {
        return versionString;
    }

    private boolean equalTimes(int t1, int t2) {
        return !(Math.abs(t2) < Math.floor(0.975 * Math.abs(t1)) && Math.abs(t2) < Math.abs(t1) - m_errlimit
                || Math.abs(t2) > Math.ceil(1.025 * Math.abs(t1)) && Math.abs(t2) > Math.abs(t1) + m_errlimit);
    }

    private boolean equalTimes(int[] t1, int offset1, int[] t2, int offset2, int duration_count) {
        for (int i = 0; i < duration_count; i++) {
            if (!equalTimes(t1[i + offset1], t2[i + offset2])) {
                if (debug)
                    System.err.print("F");
                return false;
            }
        }
        if (debug)
            System.err.print("T");
        return true;
    }

    private int calcUnit(int provUnit, int tolerance, Integer unit) {
        if (debug) {
            debugFile.println();
        
           dumpBursts();
        }
        // provUnit is a minimum value for unit
        double tot = 0;
        double intpart, fracpart;
        int nmax = 0, nmin = 0, no = 0;
        for (int i = 0; i < m_Bursts.size(); i++) {
            if (m_Bursts.get(i).type == -1) {
                for (int j = 0; j < 2; j++) {
                    double x = (double) (m_Bursts.get(i).times[j]) / provUnit + 0.5;
                    fracpart = modf(x) - 0.5;
                    tot += m_Bursts.get(i).times[j];
                    nmax += (int) x;
                }
            }
        }
        // Allow extra tolerance for very small units (e.g. pid-002A, where unit is 8u) as a small
        // change in unit is a large percentage change.
        nmin = (int) ((1 - Math.max(((double) tolerance) / 100., Math.min(3. / (double) provUnit, 0.3))) * nmax + 0.5) - 1;
        String dbg_str = String.format("prov = %d tot=%f nmin=%d nmax=%d", provUnit, tot, nmin, nmax);
        OutputDebugString(dbg_str);
        double fracmax = 0, fracminmax = 1.0;
        for (int n = nmax; n >= nmin; n--) {
            fracmax = 0;
            int testunit = (int) (tot / n + 0.5);
            for (int i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type == -1) {
                    for (int j = 0; j < 2; j++) {
                        fracpart = modf((double) m_Bursts.get(i).times[j] / (double)testunit + 0.5) - 0.5;
                        fracmax = Math.max(fracmax, Math.abs(fracpart));
                    }
                }
            }
            if (fracmax < fracminmax) {
                dbg_str = String.format("n=%d fracminmax=%f fracmax=%f testunit=%d", n, fracminmax, fracmax, testunit);
                OutputDebugString(dbg_str);
                fracminmax = fracmax;
                unit = testunit;
            }
        }
        for (int i = 0; i < m_Bursts.size(); i++) {
            // Calculate integer and fractional parts of unit multiples.
            for (int j = 0; j < 2; j++) {
                fracpart = modf((double) m_Bursts.get(i).times[j] / unit + 0.5) - 0.5;
                m_Bursts.get(i).units[j] = (int) (((double) m_Bursts.get(i).times[j]) / unit + 0.5);
                m_Bursts.get(i).fracs[j] = (short) (100 * fracpart);
                if (m_Bursts.get(i).type == -1 && Math.abs(m_Bursts.get(i).fracs[j]) > tolerance)
                    no++;
            }
        }
        dumpBursts();
        dbg_str = String.format("Unit=%d no=%d", unit, no);
        OutputDebugString(dbg_str);
        return no;
    }

    private void addBits(int bits, int num) {
        if (m_dataNdx + num > 8 * DATASIZE)
            return;
        for (int i = 0; i < num; i++) {
            m_data[m_dataNdx / 8] &= 255 - (1 << (7 - (m_dataNdx % 8)));	// Zero the bit before setting it correctly.
            m_data[m_dataNdx / 8] |= ((bits >> (num - i - 1)) & 1) << (7 - (m_dataNdx % 8));
            m_dataNdx++;
        }
    }

    private void outbits(StringBuffer str, int num) {
        while (num > 0) {
            int n = (num - 1) % (basis == 0 ? 4 : log2(basis)) + 1;
            if (str != null) {
                str.append(Integer.toHexString(m_data[0] >> (8 - n)));
            }
            for (int i = 0; i < DATASIZE; i++) {
                m_data[i] = ((m_data[i] << n | ((i < (DATASIZE - 1)) ? m_data[i + 1] >> (8 - n) : 0))) & 0xFF;
            }
            num -= n;
        }
    }

    /**
     * 
     * @param sngl_count
     * @param rpt_count
     * @param extra_count
     * @param signal_out
     * @param freq
     * @param irp
     * @return 
     */
    private int identify(int sngl_count, int rpt_count, int extra_count,
            int[] signal_out, int freq, StringBuffer irp) {
        int non_lead_count = 0;
        int offint_max = -1, offint_max_ndx = -1, offfrac_max = 0;
        int offint_min = 99999, offint_min_ndx = -1, offfrac_min = 99999;
        int onint_max = -1, onint_max_ndx = -1, onfrac_max = 0;
        int onint_min = 99999, onint_min_ndx = -1, onfrac_min = 99999;
        int burst_max = -1, burst_min = 99999;
        boolean bursts_done = false;
        int rpt_offset = 0;
        int i;

        String dbg_str;

        for (i = 0; i < m_Bursts.size(); i++) {
            if (m_Bursts.get(i).type == -1) {
                non_lead_count++;
            }
        }

        int oldunit = m_unit;
        int nonint = calcUnit(oldunit, 20, m_unit);
        if (non_lead_count <= 4) {
            // See if greater accuracy can be achieved with a unit of about one-half or one-third the size.
            nonint = calcUnit(oldunit, 8, m_unit);
            if (nonint > 0) {
                nonint = calcUnit(oldunit / 2, 15, m_unit);
            }
            if (nonint > 0) {
                nonint = calcUnit(oldunit / 3, 20, m_unit);
            }
            if (nonint > 0) {
                // Revert to original calculation if greater accuracy not achieved.
                nonint = calcUnit(oldunit, 20, m_unit);
            }
        }

        for (i = 0; i < m_Bursts.size(); i++) {
            if (m_Bursts.get(i).type == -1) {
                if (m_Bursts.get(i).units[1] > offint_max
                        || m_Bursts.get(i).units[1] == offint_max
                        && m_Bursts.get(i).fracs[1] > m_Bursts.get(offint_max_ndx).fracs[1]) {
                    offint_max = m_Bursts.get(i).units[1];
                    offint_max_ndx = i;
                }
                if (m_Bursts.get(i).units[1] < offint_min
                        || m_Bursts.get(i).units[1] == offint_min
                        && m_Bursts.get(i).fracs[1] < m_Bursts.get(offint_min_ndx).fracs[1]) {
                    offint_min = m_Bursts.get(i).units[1];
                    offint_min_ndx = i;
                }
                if (m_Bursts.get(i).units[0] > onint_max
                        || m_Bursts.get(i).units[0] == onint_max
                        && m_Bursts.get(i).fracs[0] > m_Bursts.get(onint_max_ndx).fracs[0]) {
                    onint_max = m_Bursts.get(i).units[0];
                    onint_max_ndx = i;
                }
                if (m_Bursts.get(i).units[0] < onint_min
                        || m_Bursts.get(i).units[0] == onint_min
                        && m_Bursts.get(i).fracs[0] < m_Bursts.get(onint_min_ndx).fracs[0]) {
                    onint_min = m_Bursts.get(i).units[0];
                    onint_min_ndx = i;
                }
                burst_max = Math.max(burst_max, m_Bursts.get(i).times[0] + m_Bursts.get(i).times[1]);
                burst_min = Math.min(burst_min, m_Bursts.get(i).times[0] + m_Bursts.get(i).times[1]);
                offfrac_max = Math.max(offfrac_max, (int) (m_Bursts.get(i).fracs[1]));
                offfrac_min = Math.min(offfrac_min, (int) (m_Bursts.get(i).fracs[1]));
                onfrac_max = Math.max(onfrac_max, (int) (m_Bursts.get(i).fracs[0]));
                onfrac_min = Math.min(onfrac_min, (int) (m_Bursts.get(i).fracs[0]));
            }
        }

        dbg_str = String.format("burst_max=%d burst_min=%d", burst_max, burst_min);
        OutputDebugString(dbg_str);
        dbg_str = String.format("non_lead_count=%d onint_max=%d onint_min=%d offint_max=%d offint_min=%d nonint=%d",
                non_lead_count, onint_max, onint_min, offint_max, offint_min, nonint);
        OutputDebugString(dbg_str);

        m_unit2 = 0;
        m_databits[0] = m_databits[1] = m_databits[2] = 0;
        m_biphase[0] = m_biphase[1] = m_biphase[2] = 0;
        m_RC6[0] = m_RC6[1] = m_RC6[2] = 0;

        int n_best = -1;
        double y_minmax = 99.;
        int[] outvec = new int[20];		// Size must be at least 20.
        int specsize = 0;

        m_dataNdx = 0;
        // Reset bit count bytes
        for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
            signal_out[2 * i + 1] = 0;
        }

        // Test for "gap" protocol, MARKs being equal, SPACEs being significantly separated.
        // Allow fractional rounding errors of at most 10% of the integer difference
        if (non_lead_count == 2 && onint_max == onint_min && nonint == 0 && offint_min < 50 * onint_max
                && Math.max(Math.abs(onfrac_max), Math.abs(onfrac_min)) <= 10
                && Math.max(Math.abs(offfrac_max), Math.abs(offfrac_min)) <= 10 * (offint_max - offint_min)) {
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type == -1)
                    m_Bursts.get(i).type = (i == offint_max_ndx) ? (short) 1 : (short) 0;
            }
            m_type = 1;
            bursts_done = true;
        } else if (non_lead_count == 2 && onint_max == onint_min && offint_min > 50 * onint_max) {
            int tot = 0;
            // For type 8, all times except the two data ON times are given in microseconds, not units.
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type == -1) {
                    m_Bursts.get(i).type = (i == offint_max_ndx) ? (short) 1 : (short) 0;
                    tot += m_Bursts.get(i).times[0];
                    m_Bursts.get(i).units[0] = 1;
                }
            }
            m_unit = tot / 2;
            m_type = 8;
            bursts_done = true;

            // Test for Sony-style "gap" protocol, SPACEs being equal, MARKs being significantly separated.
            // Allow fractional rounding errors of at most 10% of the integer difference
        } else if (non_lead_count == 2 && offint_max == offint_min && nonint == 0
                && Math.max(Math.abs(offfrac_max), Math.abs(offfrac_min)) <= 10
                && Math.max(Math.abs(onfrac_max), Math.abs(onfrac_min)) <= 10 * (onint_max - onint_min)) {
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type == -1)
                    m_Bursts.get(i).type = (i == onint_max_ndx) ? (short) 1 : (short) 0;
            }
            m_type = 2;
            bursts_done = true;
        } else if (non_lead_count == 2 && burst_min > 0.9 * burst_max && nonint == 0) {
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type == -1)
                    m_Bursts.get(i).type = (i == onint_max_ndx) ? (short) 1 : (short) 0;
            }
            m_type = 3;
            bursts_done = true;
        }

        if (bursts_done) {
            m_dataNdx = 0;

            for (i = 0; i < m_Bursts.size(); i++) {
                int t = m_Bursts.get(i).type;
                if (t < 2) {
                    outvec[2 * t] = m_Bursts.get(i).units[0];
                    outvec[2 * t + 1] = (m_type != 8) ? -m_Bursts.get(i).units[1] : -m_Bursts.get(i).times[1];
                }
                specsize = 2;
            }

            for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                Burst b = m_Bursts.get(signal_out[2 * i]);
                if (b.type < 100) {
                    addBits(b.type, 1);
                    signal_out[2 * i + 1] = 1;		// record number of bits added
                } else if ((m_type == 2 || m_type == 3) && i > 0) {
                    Burst b0 = m_Bursts.get(signal_out[2 * i - 2]);
                    int on = b.units[0];
                    int off = (on == onint_min) ? offint_max : (on == onint_max) ? offint_min : -1;
                    if ((b0.type < 100)
                            && (Math.abs(b.fracs[0]) <= 10)
                            && off > 0
                            && b.units[1] > off) {
                        // Lead-out following a data burst, which in types 2 (Sony-style) or 3 have a concatenated data burst.
                        addBits(on == onint_max ? 1 : 0, 1);
                        signal_out[2 * i - 1] += 1;
                        signal_out[2 * i + 1] = (on << 8) + off;	// leadout corrections
                    }
                }
            }
        }

        if (bursts_done && m_dataNdx < 4) {
            // Too few data bits to be genuine.
            bursts_done = false;
            m_type = 0;
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type < 100)
                    m_Bursts.get(i).type = -1;
            }
            // Reset the data pointer.
            m_dataNdx = 0;
            // Reset bit count bytes.
            for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                signal_out[2 * i + 1] = 0;
            }
        }

        if (!bursts_done && non_lead_count >= 3 && non_lead_count <= 5 && nonint == 0
                && offint_max <= 3 * offint_min && onint_max <= 3 * onint_min) {
            // Biphase, including RC6-style
            bursts_done = true;

            final int[] types = {200, 201, 304, 202, 203, 305, 301, 302, 303};

            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type != -1)
                    continue;
                if ((m_Bursts.get(i).units[0] % onint_min != 0) || (m_Bursts.get(i).units[1] % offint_min != 0)) {
                    bursts_done = false;
                } else {
                    m_Bursts.get(i).type = (short) types[3 * (m_Bursts.get(i).units[0] / onint_min - 1)
                            + m_Bursts.get(i).units[1] / offint_min - 1];
                }
            }

            int parity = 0;
            int last = 0;
            int sect = 0;
            int bitcount = 0;
            int RC6flag = 0;		// 0 = undetermined, 1 = provisional, 2 = is RC6, -1 = not RC6
            int RC6bit = 0;			// Bit number for RC6 T-flag (1-based, as a count)
            int[] u = null;
            boolean reset = false;
            int last1xx = -1;		// i-value of last burst of type 1xx.

            m_dataNdx = 0;
            int sectNdx = 0;

            if (bursts_done)
                for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                    switch (m_Bursts.get(signal_out[2 * i]).type) {
                        case 200:
                            bitcount++;
                            addBits((last == 201) ? 0 : 1, 1);
                            signal_out[2 * i + 1] = 1;
                            if (RC6flag == 1) {
                                RC6flag = -1;
                            }
                            break;
                        case 201:
                            if (last == 0) {
                                parity = 2;	// even parity
                            }
                            if (last == 201 && RC6flag == 0) {
                                RC6flag = 1;		// provisional
                                bitcount++;			// acts like 200
                                addBits(0, 1);
                                signal_out[2 * i + 1] = 101;	// Adding 100 signifies last added bit is RC6 bit
                                RC6bit = bitcount;
                            } else if (last == 201 || RC6flag == 1) {
                                RC6flag = -1;		// illegal biphase
                            } else {
                                bitcount += 2;
                                addBits(2, 2);
                                signal_out[2 * i + 1] = 2;
                            }
                            last = 201;
                            break;
                        case 202:
                            signal_out[2 * i + 1] = 0;	// To be sure.
                            if (last == 0) {
                                if (last1xx < 0 || m_Bursts.get(signal_out[2 * last1xx]).units[1]
                                        >= offint_min + (signal_out[2 * last1xx + 1] & 0xFF)) {
                                    parity = 1;			// odd parity
                                    bitcount++;
                                    int x = m_dataNdx - sectNdx + 1;
                                    m_dataNdx = sectNdx;
                                    addBits(0, x);		// change preceding all 1's to all 0's, with one more
                                    if (last1xx >= 0)
                                        signal_out[2 * last1xx + 1] += offint_min;
                                    signal_out[2 * i + 1] = 1;
                                } else {
                                    RC6flag = -1;		// nothing to take leading -1 from
                                }
                            }
                            if (RC6flag == 1) {
                                RC6flag = 2;		// acts like 200
                                bitcount++;
                                addBits(0, 1);
                                signal_out[2 * i + 1] = 1;
                                break;				// don't change "last"
                            } else if (last == 202) {
                                RC6flag = -1;		// illegal biphase
                            } else {
                                bitcount++;
                                addBits(1, 1);
                                signal_out[2 * i + 1] += 1;	// Will already be 1 if last=0
                            }
                            last = 202;
                            break;
                        case 203:					// behaves like 202+201
                            signal_out[2 * i + 1] = 0;	// To be sure.
                            if (last == 0) {
                                if (last1xx < 0 || m_Bursts.get(signal_out[2 * last1xx]).units[1]
                                        >= offint_min + (signal_out[2 * last1xx + 1] & 0xFF)) {
                                    last = 201;
                                    parity = 1;
                                    bitcount++;			// takes account of the half bit from the lead-in
                                    int x = m_dataNdx - sectNdx + 1;
                                    m_dataNdx = sectNdx;
                                    addBits(0, x);		// change preceding all 1's to all 0's, with one more
                                    if (last1xx >= 0)
                                        signal_out[2 * last1xx + 1] += offint_min;
                                    signal_out[2 * i + 1] = 1;
                                } else {
                                    RC6flag = -1;
                                }
                            }
                            if (last == 202 && RC6flag == 0) {
                                RC6flag = 2;		// M odd
                                bitcount++;			// acts like 200
                                addBits(1, 1);
                                signal_out[2 * i + 1] = 101;
                                RC6bit = bitcount;
                            } else if (last == 202 || RC6flag == 1) {
                                RC6flag = -1;
                            } else {
                                bitcount += 2;
                                addBits(2, 2);
                                signal_out[2 * i + 1] += 2;
                            }
                            break;
                        case 301:	// 3,-1
                            if (RC6flag == 1) {
                                last = 202;
                                RC6flag = 2;
                                bitcount++;		// acts like 202 when RC6flag is 1
                                addBits(1, 1);
                                signal_out[2 * i + 1] = 1;
                            } else
                                RC6flag = -1;
                            break;
                        case 302:	// 3,-2
                            if (RC6flag == 1) {
                                last = 201;
                                bitcount += 2;		// acts like 203
                                addBits(2, 2);
                                signal_out[2 * i + 1] = 2;
                                RC6flag = 2;
                            } else if (last == 201 && RC6flag == 0) {
                                last = 202;			// acts like 202
                                bitcount++;
                                addBits(1, 1);
                                signal_out[2 * i + 1] = 101;
                                RC6flag = 2;
                                RC6bit = bitcount;
                            } else
                                RC6flag = -1;
                            break;
                        case 303:	// 3,-3
                            if (last == 201 && RC6flag == 0) {
                                bitcount += 2;		// acts like 203
                                addBits(2, 2);
                                signal_out[2 * i + 1] = 202; // Adding 200 signifies that last but one bit is RC6 bit
                                RC6flag = 2;
                                RC6bit = bitcount - 1;
                            } else
                                RC6flag = -1;
                            break;
                        case 304:	// 1,-3
                            if (last == 0 || last == 202 && RC6flag == 0) {
                                if (last == 0)
                                    parity = 2;
                                bitcount += 2;		// acts like 201
                                addBits(2, 2);
                                signal_out[2 * i + 1] = 102;
                                RC6flag = 1;
                                RC6bit = bitcount;
                            } else
                                RC6flag = -1;
                            break;
                        case 305:	// 2,-3
                            if (last == 0 || last == 202 && RC6flag == 0) {
                                // This treats the case last==0 as even parity but in fact it could be either parity,
                                // not determined till later.  However, RC6 protocols are even parity and it is 
                                // difficult to leave parity open at this stage, so regard odd parity case here as error.
                                if (last == 0)
                                    parity = 2;
                                bitcount += 2;				// acts like 201
                                addBits(2, 2);
                                signal_out[2 * i + 1] = 202;
                                RC6flag = 2;
                                RC6bit = bitcount - 1;
                                last = 201;
                            } else
                                RC6flag = -1;
                            break;
                        default:
                            u = m_Bursts.get(signal_out[2 * i]).units;
                            if (last == 202 && u[0] == onint_min && u[1] >= offint_min) {
                                bitcount++;
                                addBits(1, 1);
                                signal_out[2 * i - 1] += 1;
                                signal_out[2 * i + 1] = (onint_min << 8) + offint_min; // Number of units of leadout used up (0x100*mark+space) 
                            } else if (last == 201 && u[0] == onint_min) {
                                signal_out[2 * i + 1] = onint_min << 8;
                            } else if (last == 201 && u[0] == 2 * onint_min && u[1] >= offint_min) {
                                bitcount++;
                                addBits(1, 1);
                                signal_out[2 * i - 1] += 1;
                                signal_out[2 * i + 1] = (2 * onint_min << 8) + offint_min;
                            } else if (last == 0 && i > last1xx + 1 && u[0] == onint_min && u[1] >= offint_min) {
                                parity = 2;		// Could also be odd parity, but treat as even.
                                bitcount++;
                                addBits(1, 1);
                                signal_out[2 * i - 1] += 1;
                                signal_out[2 * i + 1] = (onint_min << 8) + offint_min;
                            } else if (last == 0 && i > last1xx + 1 && u[0] == 2 * onint_min && u[1] >= offint_min && (last1xx < 0
                                    || m_Bursts.get(signal_out[2 * last1xx]).units[1] >= offint_min + (signal_out[2 * last1xx + 1] & 0xFF))) {
                                parity = 1;
                                bitcount += 2;
                                int x = m_dataNdx - sectNdx + 1;
                                m_dataNdx = sectNdx;
                                addBits(1, x);		// change preceding all 1's to all 0's, with one more
                                if (last1xx >= 0)
                                    signal_out[2 * last1xx + 1] += offint_min;
                                signal_out[2 * i - 1] += 2;
                                signal_out[2 * i + 1] = (2 * onint_min << 8) + offint_min;  // Number of units of leadout used up 
                            } else if (i > last1xx + 1) {
                                RC6flag = -1;
                            }
                            last1xx = i;
                            sectNdx = m_dataNdx;
                            reset = true;
                    }

                    if (RC6flag == -1 || reset && RC6flag == 1)
                        break;

                    if (i == sngl_count - 1) {
                        m_biphase[0] = parity;
                        m_databits[0] = bitcount;
                        bitcount = 0;
                        m_RC6[0] = RC6bit;
                        sect = 1;
                    } else if (i == sngl_count + rpt_count - 1) {
                        m_biphase[1] = parity;
                        m_databits[1] = bitcount;
                        bitcount = 0;
                        m_RC6[1] = RC6bit;
                        sect = 2;
                    } else if (i == sngl_count + rpt_count + extra_count - 1) {
                        m_biphase[2] = parity;
                        m_databits[2] = bitcount;
                        bitcount = 0;
                        m_RC6[2] = RC6bit;
                    }

                    if (reset) {
                        RC6flag = 0;
                        parity = 0;
                        last = 0;
                        reset = false;
                    }
                }

	           if (bursts_done && RC6flag != -1 && RC6flag != 1) {
                m_type = 4;
                outvec[0] = -offint_min;
                outvec[1] = onint_min;
                outvec[2] = onint_min;
                outvec[3] = -offint_min;
                specsize = 2;
            } else {
                m_databits[0] = m_databits[1] = m_databits[2] = 0;
                m_biphase[0] = m_biphase[1] = m_biphase[2] = 0;
                m_dataNdx = 0;
                for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                    signal_out[2 * i + 1] = 0;
                }
                m_RC6[0] = m_RC6[1] = m_RC6[2] = 0;
                if (offint_max <= 2 * offint_min && onint_max <= 2 * onint_min) {
                    // Same bursts as non-RC6 biphase but isn't biphase, eg DirecTV, so modify
                    // burst types.  This effectively encodes each bit separately, a 0 being 1
                    // unit and a 1 being 2 units, independently of whether it is a MARK or a
                    // SPACE
                    bursts_done = true;
                    for (i = 0; i < m_Bursts.size(); i++) {
                        if (m_Bursts.get(i).type == -1) {
                            bursts_done = false;
                            break;
                        }
                        if (m_Bursts.get(i).type >= 200)
                            m_Bursts.get(i).type -= 200;
                    }
                    if (bursts_done) {
                        for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                            // Each burst now encodes 2 bits
                            Burst b = m_Bursts.get(signal_out[2 * i]);
                            if (b.type < 100) {
                                signal_out[2 * i + 1] = 2;
                                addBits(b.type, 2);
                            }
                        }
                        m_type = 5;
                        outvec[0] = outvec[2] = onint_min;
                        outvec[1] = outvec[5] = -offint_min;
                        outvec[3] = outvec[7] = - 2 * offint_min;
                        outvec[4] = outvec[6] = 2 * onint_min;
                        specsize = 4;
                    }
                } else {
                    bursts_done = false;
                }
            }

            if (!bursts_done) {
                // Reset the type values just set.
                for (i = 0; i < m_Bursts.size(); i++) {
                    if (m_Bursts.get(i).type < 100 || m_Bursts.get(i).type >= 200) {
                        m_Bursts.get(i).type = -1;
                    }
                }
                // Reset the data pointer.
                m_dataNdx = 0;
                // Reset bit count bytes.
                for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                    signal_out[2 * i + 1] = 0;
                }
            }
            if (bursts_done) {
                dbg_str = String.format("RC6=%d %d %d biphase=%d %d %d databits=%d %d %d",
                        m_RC6[0], m_RC6[1], m_RC6[2],
                        m_biphase[0], m_biphase[1], m_biphase[2], m_databits[0], m_databits[1], m_databits[2]);
                OutputDebugString(dbg_str);
            }
        }

        if (!bursts_done && non_lead_count >= 3 && non_lead_count <= 16 && onint_max <= 2 * onint_min
                && Math.max(Math.abs(onfrac_max), Math.abs(onfrac_min)) <= 20) {
            // Test for arithmetic progression of SPACE times with a constant MARK time.  Allow for
            // progression to start with 0, which results in a double MARK time.
            double delta = 0;
            double x, y, y_max;
            bursts_done = true;

            // Check no other MARK times between min and max (if min and max were 2 and 4, this would
            // eliminate a possble value of 3).  Also clear type 105, set on previous loop.
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).type == -1 && (m_Bursts.get(i).units[0] % onint_min != 0)) {
                    bursts_done = false;
                }
                if (m_Bursts.get(i).type == 105)
                    m_Bursts.get(i).type = -1;
            }

            int min_time = (onint_max == onint_min) ? m_Bursts.get(offint_min_ndx).times[1] : 0;

            y_minmax = 99.;
            if (bursts_done)
                for (int n = 2; n < 16; n++) {
                    y_max = -1.;
                    delta = (double) (m_Bursts.get(offint_max_ndx).times[1] - min_time) / n;
                    for (i = 0; i < m_Bursts.size(); i++) {
                        if (m_Bursts.get(i).type == -1) {
                            x = (double) (m_Bursts.get(i).times[1] - min_time) / delta;
                            y = x - (int) (x + 0.5);
                            if (Math.abs(y) > y_max)
                                y_max = Math.abs(y);
                        }
                    }
                    if (y_max < y_minmax) {
                        y_minmax = y_max;
                        n_best = n;
                        if (Math.abs(delta - (int) (delta + 0.5)) < 0.2 * delta / n)
                            m_unit2 = (int) (delta + 0.5);
                        else
                            m_unit2 = 0;
                    }
                }

            if (y_minmax < 0.2) {
                delta = (double) (m_Bursts.get(offint_max_ndx).times[1] - min_time) / n_best;
                for (i = 0; i < m_Bursts.size(); i++) {
                    if (m_Bursts.get(i).type == -1) {
                        x = (double) (m_Bursts.get(i).times[1] - min_time) / delta;
                        m_Bursts.get(i).type = (short) (x + 0.5);
                    }
                }
            } else {
                bursts_done = false;
            }

            if (bursts_done) {
                m_type = 6;		// Default type for a.p.
            }

            if (bursts_done && onint_max == onint_min) {
                // Zero SPACE excluded.  Count the number of values in the a.p.
                if (non_lead_count == 3) {
                    // Test if this is style of Pace MSS or pid-002A, where one value occurs only before,
                    // and possibly also after, all other data bursts.
                    int t0 = -1, t1 = -1;
                    int c0 = 0, c1 = 0;	// Counts of sections all of, or all not of, bursts of type t
                    boolean is_t = false;
                    for (i = 0; i < sngl_count; i++) {
                        if (m_Bursts.get(signal_out[2 * i]).type < 100) {
                            if (t0 < 0)
                                t0 = signal_out[2 * i]; // First non-lead burst
                            if (is_t != (t0 == signal_out[2 * i])) {
                                c0++;
                                is_t = !is_t;
                            }
                        }
                    }
                    is_t = false;
                    for (i = sngl_count; i < sngl_count + rpt_count; i++) {
                        if (m_Bursts.get(signal_out[2 * i]).type < 100) {
                            if (t1 < 0)
                                t1 = signal_out[2 * i]; // First non-lead burst
                            if (is_t != (t1 == signal_out[2 * i])) {
                                c1++;
                                is_t = !is_t;
                            }
                        }
                    }

                    if (c0 < 4 && c1 < 4 && c0 + c1 > 0 && (c0 == 0 || c1 == 0 || t0 == t1)) {
                        // For both single and repeat sections, if they have data then it starts with
                        // same burst and that burst only occurs before, and possibly after, data bursts.
                        // Set this burst as type 105 and try again.
                        m_Bursts.get(Math.max(t0, t1)).type = 105;

                        for (i = 0; i < m_Bursts.size(); i++) {
                            if (m_Bursts.get(i).type < 100)
                                m_Bursts.get(i).type = -1;
                        }
                        m_type = 99;
                    } else {
                        // Not just a lead-in, so try instead for a mid-frame break, Proton style.
                        // Work from last burst downwards as a mid-frame break is probably last data burst to be found.
                        for (i = (int) m_Bursts.size() - 1; i >= 0; i--) {
                            if (m_Bursts.get(i).type < 100 && m_Bursts.get(i).sect_counts[0] < 2 && m_Bursts.get(i).sect_counts[1] < 2
                                    && m_Bursts.get(i).sect_counts[0] + m_Bursts.get(i).sect_counts[1] > 0) {
                                // Burst doesn't occur more than once in a section.
                                m_Bursts.get(i).type = 105;
                                for (i = 0; i < m_Bursts.size(); i++) {
                                    if (m_Bursts.get(i).type < 100)
                                        m_Bursts.get(i).type = -1;
                                }
                                m_type = 99;
                                break;
                            }
                        }
                    }

                    if (m_type == 6 && nonint == 0) {
                        // Test for an equal-length split between smallest two bursts and the largest burst.
                        // This is Zenith-style, which has <1,-1,1,-8|1,-10>, both the 0 and 1 having a total
                        // length of 11 units.
                        int ndx = 0;
                        int[] ndxi = new int[3];
                        for (i = 0; i < m_Bursts.size(); i++) {
                            if (m_Bursts.get(i).type < 100) {
                                if (ndx < 1 || m_Bursts.get(i).type > m_Bursts.get(ndxi[ndx - 1]).type) {
                                    ndxi[ndx] = i;
                                } else if (ndx < 2 || m_Bursts.get(i).type > m_Bursts.get(ndxi[ndx - 2]).type) {
                                    ndxi[ndx] = ndxi[ndx - 1];
                                    ndxi[ndx - 1] = i;
                                } else if (ndx < 3 || m_Bursts.get(i).type > m_Bursts.get(ndxi[ndx - 3]).type) {
                                    ndxi[ndx] = ndxi[ndx - 1];
                                    ndxi[ndx - 1] = ndxi[ndx - 2];
                                    ndxi[ndx - 2] = i;
                                }
                                ndx++;
                            } // if (m_Bursts.get(i).type < 100)
                        } // for (i=0; i<m_Bursts.size(); i++)


                        if (m_Bursts.get(ndxi[2]).units[1] == m_Bursts.get(ndxi[0]).units[1] + m_Bursts.get(ndxi[1]).units[1] + onint_min) {
                            boolean flag0 = false;
                            int first0 = -1, second0 = -1;
                            m_dataNdx = 0;
                            for (i = 0; i < sngl_count + rpt_count; i++) {
                                Burst b1 = m_Bursts.get(signal_out[2 * i]);
                                Burst b0 = null;
                                if (i > 0) {
                                    b0 = m_Bursts.get(signal_out[2 * (i - 1)]);
                                }
                                if (b1.type < 100) {
                                    ndx = signal_out[2 * i];
                                    if (ndx == ndxi[2]) {
                                        // 1-bit
                                        addBits(1, 1);
                                        signal_out[2 * i + 1] = 1;
                                        if (flag0) {
                                            // Missing second 0-burst
                                            break;
                                        }
                                    } else if (flag0) {
                                        // second burst of 0-bit
                                        flag0 = false;
                                        if (second0 == -1) {
                                            second0 = ndx;
                                        } else if (ndx != second0) {
                                            // Inconsistent ordering of bursts
                                            break;
                                        }
                                    } else {
                                        // first burst of 0-bit
                                        flag0 = true;
                                        addBits(0, 1);
                                        signal_out[2 * i + 1] = 1;
                                        if (first0 == -1) {
                                            first0 = ndx;
                                        } else if (ndx != first0) {
                                            // Inconsistent ordering of bursts
                                            break;
                                        }
                                    }
                                } else if (b0 != null && b0.type < 100) {
                                    // burst type 1xx preceded by data burst
                                    if (b1.units[0] == onint_min && !flag0) {
                                        // Lead-out acts as 1-burst
                                        addBits(1, 1);
                                        signal_out[2 * i - 1]++;
                                    } // else acts as second 0-burst, no action required
                                    signal_out[2 * i + 1] = onint_min << 8;	// record correction to lead-out on-time
                                }
                            } // for (i=0; i<sngl_count+rpt_count; i++)
                        }
                        if (i == sngl_count + rpt_count) {
                            // OK since it did not exit prematurely
                            m_type = 10;
                            outvec[0] = outvec[2] = outvec[4] = onint_min;
                            outvec[1] = m_Bursts.get(ndxi[0]).units[1];
                            outvec[3] = m_Bursts.get(ndxi[1]).units[1];
                            outvec[5] = m_Bursts.get(ndxi[2]).units[1];
                        } else {
                            // Reset bit count bytes.
                            for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                                signal_out[2 * i + 1] = 0;
                            }
                        }
                    }
                }
            }

            if (m_type == 6 && m_unit2 > 0.9 * m_unit && m_unit2 < 1.1 * m_unit /*&& n_best >= 4*/ && n_best <= 6 && nonint == 0) {
                // Test for Sejin-style protocol, <1,-3|-1,1,-2|-2,1,-1|-3,1>
                m_dataNdx = 0;
                int ndx = 0;
                for (i = 0; i < sngl_count + rpt_count; i++) {
                    Burst b1 = m_Bursts.get(signal_out[2 * i]);
                    Burst b2 = null;
                    if (i < sngl_count + rpt_count - 1) {
                        b2 = m_Bursts.get(signal_out[2 * (i + 1)]);
                    }

                    if (b1.type < 100) {
                        if (b1.units[0] == 2) {
                            if (ndx != 0)
                                break;
                            signal_out[2 * i + 1] += 1 << 8;		// Records the data 0
                            ndx = 3;
                        }
                        ndx = b1.units[1] - ndx;
                        if (ndx < 0)
                            break;
                        signal_out[2 * i + 1] += ndx;
                        ndx = 3 - ndx;
                        if (ndx < 0)
                            break;
                    }
                    if (b1.type >= 100 || i == sngl_count + rpt_count - 1) {
                        if (b2 != null && b2.type < 100) {
                            // Lead-in SPACE must be start of data, to synchronise data bursts.
                            if (b1.units[1] < 4 && Math.abs(b1.fracs[1]) < 20) {
                                ndx = b1.units[1];
                                signal_out[2 * i + 3] = (ndx + 1) << 16;	// Record data value of the lead-in
                                signal_out[2 * i + 1] = ndx;			// Set the correction to the lead-in SPACE.
                                ndx = 3 - ndx;
                            } else
                                break;
                        }
                    }
                }
                m_dataNdx = 0;
                if (i == sngl_count + rpt_count) {
                    // It's passed the tests, so set the data.
                    for (i = 0; i < sngl_count + rpt_count; i++) {
                        Burst b1 = m_Bursts.get(signal_out[2 * i]);
                        int signew = 0;
                        if (b1.type < 100) {
                            if ((signal_out[2 * i + 1] & 0xFF0000) != 0) {
                                addBits((signal_out[2 * i + 1] >> 16) - 1, 2);
                                signew += 2;
                            }
                            if ((signal_out[2 * i + 1] & 0xFF00) != 0) {
                                addBits((signal_out[2 * i + 1] >> 8 & 0xFF) - 1, 2);
                                signew += 2;
                            }
                            addBits(signal_out[2 * i + 1] & 0xFF, 2);
                            signew += 2;
                            signal_out[2 * i + 1] = signew;
                        }
                    }
                    m_type = 9;
                } else {
                    // Reset bit count bytes.
                    for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                        signal_out[2 * i + 1] = 0;
                    }
                }
            }

            if (m_type == 6 && onint_max == 2 * onint_min) {
                // This is the case of a zero SPACE time being included.
                // Bursts with a single MARK are numbered by position in the a.p. starting at 0,
                // those with double MARK have 400 added and encode 2 bits, the first being 0.
                for (i = 0; i < m_Bursts.size(); i++) {
                    if (m_Bursts.get(i).type < 100) {
                        m_Bursts.get(i).type += 400 * ((m_Bursts.get(i).units[0] == onint_max) ? 1 : 0);
                    }
                }

                for (i = 0; i < sngl_count + rpt_count; i++) {
                    signal_out[2 * i + 1] = 0;	// Just to be sure.
                }
                // Test for Grundig style, in which all pairs of bursts have the same total length.
                // The pairing can be even or odd pairs, so try both at once.
                // The values eventot and finaltot may differ from n_best if there are no zeroes in the data.
                // finaltot initially acts as oddtot.
                boolean evenOK = true, oddOK = true;
                int evensum = 0, oddsum = 0, eventot = 0, finaltot = 0, ndx = 0;
                int sectstart = 0;
                int last1xx = -1; // Stores index of lead-in for re-use.
                Burst b1 = null;
                Burst b2 = null;
                for (i = 0; i < sngl_count + rpt_count; i++) {
                    b1 = m_Bursts.get(signal_out[2 * i]);
                    b2 = (i < sngl_count + rpt_count - 1) ? m_Bursts.get(signal_out[2 * (i + 1)]) : null;
                    int t = b1.type;

                    if ((ndx & 1) != 0 && t < 100) {
                        evensum += t;
                        oddsum = t;
                        if (eventot == 0)
                            eventot = evensum;
                        else if (evensum != eventot)
                            evenOK = false;
                        ndx++;
                    } else if ((ndx & 1) != 0 && t >= 400) {
                        if (eventot == 0)
                            eventot = evensum;
                        else if (evensum != eventot)
                            evenOK = false;
                        evensum = oddsum = t % 100;
                        if (finaltot == 0)
                            finaltot = oddsum;
                        else if (oddsum != finaltot)
                            oddOK = false;
                        signal_out[2 * i + 1] = 100 + t % 100;
                        ndx += 2;
                    } else if ((ndx & 1) == 0 && t < 100) {
                        evensum = t;
                        oddsum += t;
                        if (finaltot == 0)
                            finaltot = oddsum;
                        else if (oddsum != finaltot)
                            oddOK = false;
                        signal_out[2 * i + 1] = 100 + t;
                        ndx++;
                    } else if ((ndx & 1) == 0 && t >= 400) {
                        if (finaltot == 0)
                            finaltot = oddsum;
                        else if (oddsum != finaltot)
                            oddOK = false;
                        oddsum = evensum = t % 100;
                        if (eventot == 0)
                            eventot = evensum;
                        else if (evensum != eventot)
                            evenOK = false;
                        signal_out[2 * i + 1] = 100;
                        ndx += 2;
                    }

                    if (t >= 100 && t < 200 || i == sngl_count + rpt_count - 1) {
                        if (!evenOK && !oddOK) {
                            bursts_done = false;
                            break;
                        }
                        if (ndx > 0 && (evenOK && (ndx & 1) != 0 || oddOK && (ndx & 1) == 0)) {
                            // Last encoded bit is 0.  To be valid, a (1,-0) burst must be included in the lead-out;
                            if (b1.units[0] < 2) {
                                if ((ndx & 1) == 0)
                                    evenOK = false;
                                else
                                    oddOK = false;
                            } else {
                                signal_out[2 * i + 1] = 1 << 8;	// Subtract 1 from the MARK units.
                                if (oddOK)
                                    signal_out[2 * i - 1] = 0;		// Record the data bit, only needed for odd parity.
                            }
                        }

                        if (ndx > 0 && evenOK && !oddOK) {
                            // Complement the intermediate data values for this section.
                            for (int j = sectstart; j < i; j++) {
                                if (signal_out[2 * j + 1] >= 100) {
                                    signal_out[2 * j + 1] = 200 + eventot - signal_out[2 * j + 1];
                                }
                            }
                            if (last1xx >= 0)
                                signal_out[2 * last1xx + 1] = 0; // Cancel corrections set for odd parity case.
                        }
                        // Set up for next data section.
                        ndx = 0;
                        if (b2 != null) {
                            evenOK = oddOK = true;
                            if (b2.type < 100 || b2.type >= 400) {
                                // Next burst is a data burst.  For odd parity this means that this
                                // burst must be a data burst with an increased MARK time.  Test for this. 
                                x = (double) (b1.times[1]) / delta;
                                oddsum = (int) (x + 0.5);	// If not a valid value the next sum check will fail.
                                if (Math.abs(x - oddsum) > 0.2)
                                    oddOK = false;
                                if (b1.times[0] < 1.1 * onint_min * m_unit)
                                    oddOK = false;
                                else
                                    signal_out[2 * i + 1] = (1 << 8) + oddsum;	// Corrections to be applied.
                                last1xx = i;	// Save index, as need to cancel corrections if even parity.
                            }
                            sectstart = i + 1;
                        }
                    }
                    dbg_str = String.format("i=%d evenOK=%d oddOK=%d eventot=%d finaltot=%d", i, evenOK, oddOK, eventot, finaltot);
                    OutputDebugString(dbg_str);

                } // for (i=0; i<sngl_count+rpt_count; i++) 

                if (bursts_done) {
                    if (!oddOK)
                        finaltot = eventot;

                    for (i = 0; i < 20; i++) {
                        outvec[i] = 0;
                    }

                    for (i = 0; i < sngl_count + rpt_count; i++) {
                        b1 = m_Bursts.get(signal_out[2 * i]);
                        ndx = signal_out[2 * i + 1] - 100;
                        if ((b1.type < 100 || b1.type >= 400) && ndx >= 0)
                            outvec[ndx]++;
                    }
                    specsize = 0;
                    for (i = 0; i < 16; i++) {
                        if (outvec[i] > 0)
                            specsize++;
                    }

                    if (specsize > 4)
                        bursts_done = false;
                }

                if (bursts_done) {
                    int j = 16;
                    for (i = 0; i < 16; i++) {
                        if (outvec[i] > 0)
                            outvec[j++] = i;
                    }
                    dbg_str = String.format("outvec16+ %d %d %d %d specsize %d",
                            outvec[16], outvec[17], outvec[18], outvec[19], specsize);
                    OutputDebugString(dbg_str);

                    int inserted = -1;
                    if (specsize == 3) {
                        // Try to fill in the gap.  Doing so may improve the decode
                        // but it doesn't make any other difference.
                        if (outvec[0] == 0) {
                            inserted = 0;
                        } else if ((finaltot & 1) == 0 && outvec[finaltot / 2] == 0) {
                            inserted = finaltot / 2;
                        } else if ((finaltot & 1) != 0 && outvec[finaltot / 2] == 0 && outvec[(finaltot + 1) / 2] == 0) {
                            inserted = finaltot / 2;
                        } else if (outvec[finaltot - outvec[17]] == 0) {
                            inserted = finaltot - outvec[17];
                        } else if (outvec[finaltot - outvec[18]] == 0) {
                            inserted = finaltot - outvec[18];
                        }
                        if (inserted >= 0) {
                            outvec[inserted] = 1;
                            specsize++;
                        }
                        if (specsize == 4) {
                            // Gap filled, so recalculate.
                            int j1 = 16;
                            for (i = 0; i < 16; i++) {
                                if (i == inserted)
                                    inserted = j1 - 16;
                                if (outvec[i] > 0)
                                    outvec[j1++] = i;
                            }
                        }
                    }
                    dbg_str = String.format("inserted = %d", inserted);
                    OutputDebugString(dbg_str);
                    for (i = 0; i < 16; i++) {
                        outvec[i] = -1;
                    }
                    if (nonint == 0) {
                        for (j = 0; j < specsize; j++) {
                            outvec[4 * j] = outvec[4 * j + 2] = onint_min;
                            for (i = 0; i < m_Bursts.size(); i++) {
                                if (outvec[4 * j + 3] < 0 && m_Bursts.get(i).type == outvec[j + 16]) {
                                    outvec[4 * j + 3] = m_Bursts.get(i).units[1];
                                }
                                if (outvec[4 * j + 3] < 0 && m_Bursts.get(i).type == 400 + outvec[j + 16]) {
                                    outvec[4 * j + 3] = m_Bursts.get(i).units[1];
                                }
                                if (outvec[4 * j + 1] < 0 && m_Bursts.get(i).type == finaltot - outvec[j + 16]) {
                                    outvec[4 * j + 1] = m_Bursts.get(i).units[1];
                                }
                                if (outvec[4 * j + 1] < 0 && m_Bursts.get(i).type == 400 + finaltot - outvec[j + 16]) {
                                    outvec[4 * j + 1] = m_Bursts.get(i).units[1];
                                }
                            }
                            if (outvec[4 * j + 3] < 0 && outvec[j + 16] == 0) {
                                outvec[4 * j + 3] = 0;
                            }
                            if (outvec[4 * j + 1] < 0 && outvec[j + 16] == finaltot) {
                                outvec[4 * j + 1] = 0;
                            }
                        }
                        // Now try to fill in values that are actually unused.  The values are
                        // calculated and actually immaterial but the make the decode look better.
                        j = inserted;
                        if (outvec[4 * j + 1] < 0 && outvec[4 * j + 3] < 0 && j > 0 && j < 3
                                && outvec[4 * j - 3] > outvec[4 * j + 5] + 1
                                && outvec[4 * j - 1] < outvec[4 * j + 7] - 1) {
                            outvec[4 * j + 1] = (outvec[4 * j - 3] + outvec[4 * j + 5]) / 2;
                            outvec[4 * j + 3] = outvec[4 * j - 1] + outvec[4 * j + 7] - outvec[4 * j + 1];
                        }
                        if (specsize > 1 && outvec[1] < 0 && outvec[3] >= 0
                                && outvec[5] >= 0 && outvec[7] >= 0) {
                            outvec[1] = outvec[5] + outvec[7] - outvec[3];
                        }
                        j = specsize - 1;
                        if (specsize > 1 && outvec[4 * j + 3] < 0 && outvec[4 * j + 1] >= 0
                                && outvec[4 * j - 1] >= 0 && outvec[4 * j - 3] >= 0) {
                            outvec[4 * j + 3] = outvec[4 * j - 1] + outvec[4 * j - 3] - outvec[4 * j + 1];
                        }
                    } else {
                        // Treat as invalid, but it would be better to give a different bitspec.
                        bursts_done = false;
                    }
                }

                if (bursts_done) {
                    m_dataNdx = 0;
                    for (i = 0; i < sngl_count + rpt_count; i++) {
                        b1 = m_Bursts.get(signal_out[2 * i]);
                        if (b1.type < 100 || b1.type >= 400 && signal_out[2 * i + 1] >= 100) {
                            for (int j = 0; j < specsize; j++) {
                                if (outvec[16 + j] == signal_out[2 * i + 1] - 100) {
                                    addBits(j, 2);
                                    signal_out[2 * i + 1] = 2;
                                }
                            }
                        }
                    }
                }

                if (bursts_done) {
                    m_type = 7;
                } else {
                    int fourplus = 0; // Count of burst types with off-time 4 units or more
                    int fourplusndx = 0;
                    for (i = 0; i < m_Bursts.size(); i++) {
                        if (m_Bursts.get(i).type > 3 && m_Bursts.get(i).type < 100) {
                            fourplus++;
                            fourplusndx = i;
                        }
                        if (m_Bursts.get(i).type < 100) {
                            m_Bursts.get(i).type += 400 * ((m_Bursts.get(i).units[0] == onint_max) ? 1 : 0);
                        }
                    }
                    if (m_type != 99 && fourplus == 1) {
                        // Test for potential RC5x style
                        m_type = 99;
                        for (i = 0; i < 3; i++) {
                            if (m_Bursts.get(fourplusndx).sect_counts[i] > 1)
                                m_type = 0;
                        }
                        // Type 105 is the internal dividing burst in RC5x style.
                        if (m_type == 99) {
                            m_Bursts.get(fourplusndx).type = 105;
                        }
                    }
                }
            }

            if (m_type == 6 && m_unit2 > 0 && onint_min == onint_max) {
                int n = (n_best < 4) ? 2 : (n_best < 8) ? 3 : 4; // bits per burst
                for (i = 0; i < sngl_count + rpt_count; i++) {
                    int t = m_Bursts.get(signal_out[2 * i]).type;
                    if (t < 100) {
                        addBits(t, n);
                        signal_out[2 * i + 1] = n;
                    }
                }
                specsize = (n_best < 4) ? 4 : (n_best < 8) ? 8 : 16;
            } else if (m_type == 6) {
                bursts_done = false;
            }

            if (!bursts_done) {
                // Reset the type values just set (except 105).
                for (i = 0; i < m_Bursts.size(); i++) {
                    if (m_Bursts.get(i).type < 100 || m_Bursts.get(i).type >= 200) {
                        m_Bursts.get(i).type = -1;
                    }
                }
                // Reset the data pointer.
                m_dataNdx = 0;
                // Reset bit count bytes.
                for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                    signal_out[2 * i + 1] = 0;
                }
                if (m_type != 99)
                    m_type = 0;
            }
        } // end of a.p. testing

        if (n_best > 0) {
            dbg_str = String.format("n_best=%d m_unit2=%d y_minmax=%f m_type=%d\n", n_best, m_unit2, y_minmax, m_type);
            OutputDebugString(dbg_str);
        }

        /*if (bursts_done)*/ for (i = 0; i < m_Bursts.size(); i++) {
            dbg_str = String.format("Burst %d: On=%d/%d Off=%d/%d Type=%d s_count=%d r_count=%d e_count=%d", i,
                    m_Bursts.get(i).units[0], m_Bursts.get(i).fracs[0], m_Bursts.get(i).units[1], m_Bursts.get(i).fracs[1],
                    m_Bursts.get(i).type, m_Bursts.get(i).sect_counts[0], m_Bursts.get(i).sect_counts[1], m_Bursts.get(i).sect_counts[2]);
            OutputDebugString(dbg_str);
            if (m_Bursts.get(i).cumulative[0] + m_Bursts.get(i).cumulative[1] + m_Bursts.get(i).cumulative[2] > 0) {
                dbg_str = String.format("Cumulations %d %d %d\n", m_Bursts.get(i).cumulative[0],
                        m_Bursts.get(i).cumulative[1], m_Bursts.get(i).cumulative[2]);
                OutputDebugString(dbg_str);
            }
        }

        if (bursts_done && m_type > 0 && rpt_count > 0) {
            int rpt_start_type = m_Bursts.get(signal_out[2 * sngl_count]).type;
            int rpt_end_type = m_Bursts.get(signal_out[2 * (sngl_count + rpt_count - 1)]).type;
            if (rpt_start_type >= 100 && rpt_start_type < 200
                    && !(rpt_end_type >= 100 && rpt_end_type < 200)) {
                // Repeat section starts with a control burst but ends with a data burst.
                // This suggests that the repeat section has been misidentified by one burst
                // position as it should end in a lead-out control burst.  Return 1 instead of
                // 0 to signify this.
                rpt_offset = 1;
            }
        }

        if (!bursts_done && m_type == 0 && sngl_count == 0 && rpt_count > 1 && m_unit > 700) {
            // Treat as some form of asynchronous protocol.  The unit for such protocols generally seems more
            // than 700u.  First check that all fractional parts, lead-ins included, are <= 10 and all
            // unit parts are <= 12 (Lutron start bits can extend to 11 units).
            for (i = 0; i < m_Bursts.size(); i++) {
                if (m_Bursts.get(i).fracs[0] > 10 || m_Bursts.get(i).units[0] > 12)
                    break;
                if (m_Bursts.get(i).type != 101 && (m_Bursts.get(i).fracs[1] > 10 || m_Bursts.get(i).units[1] > 12))
                    break;
            }
            if (i == m_Bursts.size()) {
                // Find total number of bits, = total of units
                int tot = 0;
                m_dataNdx = 0;
                Burst b;
                for (i = 0; i < rpt_count - 1; i++) {
                    b = m_Bursts.get(signal_out[2 * i]);
                    if (b.type > 100)
                        break;
                    tot += b.units[0] + b.units[1];
                    addBits(0xFF, b.units[0]);
                    addBits(0, b.units[1]);
                    signal_out[2 * i + 1] = b.units[0] + b.units[1];
                    if (tot > 40)
                        break;
                }
                b = m_Bursts.get(signal_out[2 * (rpt_count - 1)]);
                tot += b.units[0];
                addBits(0xFFFF, b.units[0]);
                signal_out[2 * rpt_count - 3] += b.units[0];
                signal_out[2 * rpt_count - 1] = b.units[0] << 8;
                if (i == rpt_count - 1 && tot < 40) {
                    m_type = 11;
                    bursts_done = true;
                } else {
                    m_dataNdx = 0;
                    // Reset bit count bytes.
                    for (i = 0; i < sngl_count + rpt_count + extra_count; i++) {
                        signal_out[2 * i + 1] = 0;
                    }
                }
            }
        }

        if (irp != null)
            irp.replace(0, irp.length(), "Undetermined");
        if (irp != null && bursts_done) {
            int d_ndx = 0;
            int v_ndx = 0;
            int skip = 0;
            
            if (bursts_done) {
                //int f = (freq + 50) / 100;
                //int n = 
                //irp = new StringBuffer(String.format("{%d.%.1dk,", f / 10, f % 10));
                if (irp.length() > 0)
                    irp.replace(0, irp.length(), "");
                irp.append(String.format("{%3.1fk,", (double)freq / 1000));
                irp.append(String.format((m_type == 6) ? "msb}<" : "%d,msb}<", m_unit));
                if (m_type <= 5) {
                    for (i = 0; i < specsize; i++) {
                        irp.append(String.format("%d,%d|", outvec[2 * i], outvec[2 * i + 1]));
                    }
                } else if (m_type == 6) {
                    irp.append(String.format("-%du|%du,-%du>(<", m_unit2, m_Bursts.get(offint_min_ndx).times[0], m_Bursts.get(offint_min_ndx).times[1]));
                    for (i = 0; i < specsize; i++) {
                        irp.append(String.format("1:-%d|", i + 1));
                    }
                } else if (m_type == 7) {
                    for (i = 0; i < specsize; i++) {
                        irp.append(String.format("%d,-%d,%d,-%d|", outvec[4 * i], outvec[4 * i + 1], outvec[4 * i + 2], outvec[4 * i + 3]));
                    }
                } else if (m_type == 8) {
                    for (i = 0; i < specsize; i++) {
                        irp.append(String.format("%d,%du|", outvec[2 * i], outvec[2 * i + 1]));
                    }
                } else if (m_type == 9) {
                    irp.append(String.format("1,-3|-1,1,-2|-2,1,-1|-3,1|"));
                } else if (m_type == 10) {
                    irp.append(String.format("%d,-%d,%d,-%d|%d,-%d|", outvec[0], outvec[1], outvec[2], outvec[3], outvec[4], outvec[5]));
                } else if (m_type == 11) {
                    irp.append(String.format("-1|1|"));
                }

                //n += sprintf(irp+n-1, ">(")-1;
                chop(irp);
                irp.append(">(");
                for (i = 0; i < sngl_count + rpt_count; i++) {
                    if (i == sngl_count - rpt_count && rpt_count > 0 && m_type != 6 // Units not used for type 6
                            && m_Bursts.get(signal_out[2 * i]).type >= 100 && m_Bursts.get(signal_out[2 * i]).type < 200
                            && m_Bursts.get(signal_out[2 * sngl_count]).type >= 100 && m_Bursts.get(signal_out[2 * sngl_count]).type < 200
                            && m_Bursts.get(signal_out[2 * i]).units[0] > m_Bursts.get(signal_out[2 * sngl_count]).units[0]
                            && m_Bursts.get(signal_out[2 * i]).units[1] == m_Bursts.get(signal_out[2 * sngl_count]).units[1]) {
                        // Test if single section differs from repeat section only by having the longer
                        // initial MARK that has just been identified.
                        boolean eq = true;
                        for (int j = i + 1; j < sngl_count; j++) {
                            if (signal_out[2 * j] != signal_out[2 * (j + rpt_count)])
                                eq = false;
                        }
                        if (eq) {
                            skip = 0;
                            irp.append(String.format("%d,", m_Bursts.get(signal_out[2 * i]).units[0] - m_Bursts.get(signal_out[2 * sngl_count]).units[0]));
                            for (int j = i + 1; j < sngl_count; j++) {
                                Burst b = m_Bursts.get(signal_out[2 * j]);
                                if (b.type < 100 || b.type >= 200)
                                    skip += signal_out[2 * j + 1] % 100;
                            }
                            i = sngl_count;		// Skip the irp forward
                            if (skip > 0) {
                                m_datacounts[v_ndx++] = -skip;	// Record the number of bits to skip.
                            }
                        }
                    }

                    Burst b = m_Bursts.get(signal_out[2 * i]);
                    int bitcode = signal_out[2 * i + 1];

                    if (i == sngl_count && sngl_count > 0 && rpt_count > 0)
                        irp.append("(");

                    if (m_type != 11 && (b.type < 100 || b.type >= 200) || m_type == 11 && i < rpt_count - 1) {
                        if (bitcode / 100 > 0) {
                            d_ndx += bitcode % 100;
                            //irp[n++] = 'A'+v_ndx-(skip>0);
                            irp.append(String.format("%c", (int) 'A' + v_ndx - ((skip > 0) ? 1 : 0)));
                            irp.append(String.format(":%d,", d_ndx - (bitcode / 100)));
                            m_datacounts[v_ndx++] = d_ndx - (bitcode / 100);
                            irp.append(String.format("<-%d,%d|%d,-%d>(", 2 * offint_min, 2 * onint_min, 2 * onint_min, 2 * offint_min));
                            irp.append(String.format("%c", (int) 'A' + v_ndx - (skip > 0 ? 1 : 0)));
                            irp.append(String.format(":1),"));
                            m_datacounts[v_ndx++] = 1;
                            d_ndx = bitcode / 100 - 1;
                        } else
                            d_ndx += bitcode % 100;
                    }

                    if (m_type != 11 && b.type >= 100 && b.type < 200 || i == sngl_count - 1 || i == sngl_count + rpt_count - 1) {
                        if (d_ndx > 0 && v_ndx < 26) {
                            irp.append(String.format("%c", (int) 'A' + v_ndx - (skip > 0 ? 1 : 0)));
                            irp.append(String.format(":%d,", d_ndx));
                            m_datacounts[v_ndx++] = d_ndx;
                        }
                        d_ndx = 0;
                    }

                    if (m_type != 11 && b.type >= 100 && b.type < 200 || m_type == 11 && i == rpt_count - 1) {
                        boolean tflag = (m_type == 6 || m_type == 8);	// Unit not displayed if type 6 and barely used in type 8
                        // Attempt to correct simple ratios of on/off times in non-data bursts, e.g 17:8 is probably 16:8
                        int t0 = b.units[0] - (signal_out[2 * i + 1] >> 8);
                        int t1 = b.units[1] - (signal_out[2 * i + 1] & 0xFF);
                        boolean t0chk = false;
                        boolean t1chk = false;
                        float ratio = (float) (t0) / t1;
                        if (t0 > t1 && t1 > 0 && t1 <= 10 * offint_min && Math.abs(b.fracs[1] * offint_min) <= Math.max(5 * t1, 20 * offint_min)
                                && (ratio < 4.5)) {
                            int x = (int) (ratio + 0.5);
                            float y = ratio / x - 1;
                            if (Math.abs(y) < 0.1) {
                                t0 = x * t1;
                                t0chk = t1chk = true;
                            }
                        } else if (t1 > t0 && t0 > 0 && t0 <= 10 * onint_min && Math.abs(b.fracs[0] * onint_min) <= Math.max(5 * t0, 20 * onint_min)
                                && (1/ratio < 4.5)) {
                            int x = (int) (1/ratio + 0.5);
                            float y = 1/ratio - x;
                            if (Math.abs(y) < 0.2) {
                                t1 = x * t0;
                                t0chk = t1chk = true;
                            }
                        }

                        if (!t0chk && t0 <= 20 * onint_min && Math.abs(b.fracs[0] * onint_min) <= Math.max(5 * t0, 20 * onint_min)) {
                            t0chk = true;
                        }
                        if (!t1chk && t1 <= 20 * offint_min && Math.abs(b.fracs[1] * offint_min) <= Math.max(5 * t1, 20 * offint_min)) {
                            t1chk = true;
                        }

                        if (t0 > 0) {
                            if (!tflag && t0chk) {
                                irp.append(String.format("%d,", t0));
                            } else {
                                tflag = true;
                                t0 = (int) ((t0 + ((double) b.fracs[0] / 100)) * m_unit + 0.5);
                                if (t0 < 10000) {
                                    irp.append(String.format("%du,", t0));
                                } else {
                                    t0 = (t0 + 50) / 100;
                                    irp.append(String.format("%d.%1dm,", t0 / 10, t0 % 10));
                                }
                            }
                        }
                        if (b.type == 103) {
                            irp.append(String.format("^%dm,", (b.cumulative[i >= sngl_count ? 1 : 0] + 500) / 1000));
                        } else if (t1 > 0) {
                            if (!tflag && t1chk) {
                                irp.append(String.format("-%d,", t1));
                            } else {
                                t1 = (int) ((t1 + ((double) b.fracs[1] / 100)) * m_unit + 0.5);
                                if (t1 < 10000) {
                                    irp.append(String.format("-%du,", t1));
                                } else {
                                    t1 = (t1 + 50) / 100;
                                    irp.append(String.format("-%d.%1dm,", t1 / 10, t1 % 10));
                                }
                            }
                        }
                    }

                    if (rpt_count > 0 && i == sngl_count + rpt_count - 1) {
                        //n += sprintf(irp+n-1, ")+")-1;
                        chop(irp);
                        irp.append(")+");
                    }
                    if (sngl_count > 0 && i == sngl_count + rpt_count - 1) {
                        //n += sprintf(irp+n-(rpt_count==0), ")")-(rpt_count==0);
                        if (rpt_count == 0)
                            chop(irp);
                        irp.append(")");
                    }
                }
                if (m_type == 6) {
                    irp.append(")");
                }
                if (v_ndx > 0) {
                    skip = 0;
                    irp.append("{");
                    for (i = 0; i < v_ndx; i++) {
                        if (m_datacounts[i] >= 0) {	// though =0 should not happen
                            irp.append(String.format("%c", (int) 'A' + i - skip));
                            irp.append(String.format("=%s", basisPrefix(basis)));
                            //n += outbits(irp+n, m_datacounts[i]);
                            outbits(irp, m_datacounts[i]);
                            irp.append(String.format((i == v_ndx - 1) ? "}" : ","));
                        } else {
                            outbits(null, -m_datacounts[i]);	// Skip these bits.
                            skip++;
                        }
                    }
                }
                //if (!m_altLeadout.isEmpty()  && rpt_count > 0 && (m_type < 3 || m_type > 4 && m_type < 7)) {
                    // Other types have all bits of constant length, so cumulative leadout is irrelevant.
                //    irp.append(m_altLeadout);
                //}
                if (!( rpt_count > 0 && (m_type < 3 || m_type > 4 && m_type < 7)))
                    m_altLeadout = "";
            }
            OutputDebugString(new String(irp));
        }
        if (debug) {
            debugFile.println(rpt_offset);

            dumpBursts();
        }
        return rpt_offset;
    }
    
    /**
     * Mark burst as specified type if it never occurs other than as a lead-in, occurs as such at least once,
     * and has either a longer burst length or a longer MARK time than any other untyped burst.
     * 
     * @param sngl_count
     * @param rpt_count
     * @param extra_count
     * @param signal_out
     * @param li
     * @param type
     * @return 
     */
    private boolean seekLeadin(int sngl_count, int rpt_count, int extra_count, int[] signal_out, int li, int type) {
        boolean newleadin;
        boolean found = false;
        do {
            newleadin = false;
            for (int i = 0; i < m_Bursts.size(); i++) {
                short[] c = m_Bursts.get(i).sect_counts;
                if (m_Bursts.get(i).type == -1
                        && (c[0] == 0 || sngl_count > li && c[0] == 1 && i == signal_out[2 * li])
                        && (c[1] == 0 || rpt_count > li && c[1] == 1 && i == signal_out[2 * (sngl_count + li)])
                        && (c[2] == 0 || extra_count > li && c[2] == 1 && i == signal_out[2 * (sngl_count + rpt_count + li)])
                        && c[0] + c[1] + c[2] > 0) {
                    int m0 = 0, mm = 0;
                    for (int j = 0; j < m_Bursts.size(); j++) {
                        if (j != i && m_Bursts.get(j).type == -1) {
                            mm = Math.max(mm, m_Bursts.get(j).times[0] + m_Bursts.get(j).times[1]);
                            m0 = Math.max(m0, m_Bursts.get(j).times[0]);
                        }
                    }
                    if (m_Bursts.get(i).times[0] + m_Bursts.get(i).times[1] > mm || m_Bursts.get(i).times[0] > m0) {
                        m_Bursts.get(i).type = (short) type;
                        newleadin = true;
                        found = true;
                        break;
                    }
                }
            }
        } while (newleadin);
        String dbg_str = String.format("SeekLeadIn type %d, result=%b", type, found);
        OutputDebugString(dbg_str);
        return found;
    }
    
    private static void printArray(int[] array) {
        for (int i = 0; i < array.length; i++) 
            System.out.println(i + "\t" + array[i]);
    }
    
    private static void printArray2(int[][] array) {
        for (int i = 0; i < array.length; i++) 
            System.out.println(i + "\t" + array[i][0] + "\t" + array[i][1]);
    }
    
    /**
     * Mainly for debugging. Takes CCF as input arguments.
     * @param args 
     */
    public static void main(String[] args) {
        IrSignal irSignal = null;
        Analyzer analyzer = null;
        if (args.length == 0) {
            int times[] = {
               /* +9024, -4512, +564, -1692, +564, -564, +564, -564, +564, -564, +564, -564, +564, -564,
                +564, -564, +564, -564, +564, -564, +564, -1692, +564, -564, +564, -564, +564, -564, +564, -564,
                +564, -564, +564, -564, +564, -1692, +564, -1692, +564, -564, +564, -564, +564, -564, +564, -564,
                +564, -564, +564, -564, +564, -564, +564, -564, +564, -1692, +564, -1692, +564, -1692, +564, -1692,
                +564, -1692, +564, -1692, +564, -43992,
                +9024, -2256, +564, -97572*/
            +895, -895, +895, -2685, +895, -895, +895, -895, +895, -895, +895, -895,
            +895, -895, +895, -895, +895, -895, +895, -895, +895, -895, +895, -895, +895, -895, +895, -88045
            };
            //int begLength = 34;
            int begLength=0;
            //int endLength=2;
            int endLength=14;
            analyzer = new Analyzer(times, begLength, endLength, 0, 1, 38400, 100, 0, true);
            //printArray(analyzer.getSignal());
            //printArray(analyzer.getSignalAsBurstIndices());
            //printArray2(analyzer.getBursts());
            //System.out.println(analyzer.getIrp());
            irSignal = new IrSignal(times, begLength, endLength, 38400);

        } else {
            int[] ccf = new int[args.length];
            for (int i = 0; i < args.length; i++) {
                ccf[i] = Integer.parseInt(args[i], 16);
            }
            try {
                irSignal = Pronto.ccfSignal(ccf);
            } catch (IrpMasterException ex) {
                System.err.println(ex.getMessage());
            }
        }
        
        if (analyzer == null)
            analyzer = new Analyzer(irSignal, 100, 0, true);
        printArray(analyzer.getSignal());
        printArray(analyzer.getSignalAsBurstIndices());
        printArray2(analyzer.getBursts());
        System.out.println(analyzer.getIrpWithAltLeadout());
    }
}
