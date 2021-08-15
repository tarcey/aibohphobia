/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aibohphobia;

import java.math.BigInteger;
import java.util.Random;

/**
 *
 * @author taw
 */


/*
 * Aibophobia is the irrational fear of palindromes. It's also a palindrome.
 *
 * This project aims to create a reversible programming language where correct
 * syntax guarantees reversibility, such that no runtime checks need to be done to check for reversibility.
 *
 * TODO:
 *   - basic math CHECK
 *   - pointers CHECK
 *   - comparators
 *   - conditional jumps
 */
public class Aibohphobia {
    private final static Random RND = new Random(101);

    //Allowed operators in test programs. See function random_line
    private final static String FS = "+x*-i";//"+x*-i"; //Multiplicative functions tend to throw division-by-zero errors eventually


    //Returns a random valid line
    private final static String random_line(final int mem_size){
        int rnd = RND.nextInt(5);
        String out = ""+FS.charAt(rnd);

        switch (rnd) {
            case 0:
                if(RND.nextBoolean())
                    out+=""+RND.nextInt(mem_size)+","+RND.nextInt(mem_size);
                else
                    out+=""+RND.nextInt(mem_size)+","+RND.nextInt(mem_size)+","+RND.nextInt(mem_size);
                break;
            case 1:
                switch(RND.nextInt(4)){
                    case 0:
                        out+=""+RND.nextInt(mem_size)+","+RND.nextInt(mem_size); break;
                    case 1:
                        out+="|"+RND.nextInt(mem_size)+"|,"+RND.nextInt(mem_size)+""; break;
                    case 2:
                        out+=""+RND.nextInt(mem_size)+",|"+RND.nextInt(mem_size)+"|"; break;
                    case 3:
                        out+="|"+RND.nextInt(mem_size)+"|,|"+RND.nextInt(mem_size)+"|"; break;
                    default: break;
                }
                break;
            case 2:
                if(RND.nextBoolean())
                    out+=""+RND.nextInt(mem_size)+","+RND.nextInt(mem_size);
                else
                    out+=""+RND.nextInt(mem_size)+","+RND.nextInt(mem_size)+","+RND.nextInt(mem_size);
                break;
            case 3:
                out+=""+RND.nextInt(mem_size);
                break;
            case 4:
                out+=""+RND.nextInt(mem_size);
                break;
            default:
                break;
        }
        return out+FS.charAt(rnd)+"\n";
    }

    //Returns a random valid program
    private static String random_program(final int num_lines, final int mem_size){
        String out = "O\n";
        for(int i=0;i<num_lines;i++){
            out += random_line(mem_size);
        }
        return out+"O\n";
    }

    //Returns memory containing random numbers
    private static BalancedTernary[] random_mem(final int size, int lower_bound, int upper_bound){
        BalancedTernary[] out = new BalancedTernary[size];
        for(int i=0;i<size;i++){
            out[i] = new BalancedTernary(RND.nextInt(upper_bound-lower_bound)+lower_bound);
        }
        return out;
    }
    private static BalancedTernary[] random_mem(final int size, BigInteger lower_bound, BigInteger upper_bound){
        BalancedTernary[] out = new BalancedTernary[size];
        BigInteger range = upper_bound.subtract(lower_bound);
        for(int i=0;i<size;i++){
            BigInteger rand = lower_bound.add(new BigInteger(range.bitLength()*5+1, RND).mod(range));
            out[i] = new BalancedTernary(rand);
        }
        return out;
    }

    //Torture test by running a random program. Checks for reversibility faults.
    public static void main1(String[] args) throws SyntaxError{


        final int prgrm_length = 1000;
        final int mem_size=500;
        final int repetitions=10;

        String rnd_prgram = random_program(prgrm_length, mem_size);
        InstrumentedProcess p = new InstrumentedProcess(rnd_prgram, random_mem(mem_size, BalancedTernary.MOD.negate(), BalancedTernary.MOD));//
        p.set_verbose(false);

        for(int i=0;i<repetitions;i++){
            if(RND.nextBoolean())
                p.reverse_direction();
            if(RND.nextBoolean())
                p.run();
            else
                p.run(RND.nextInt((int)Math.sqrt(prgrm_length)));
            System.out.print(".");
        }

        System.out.println("Number of reversibility faults:\t"+p.get_fault_count());
    }

    //experimentation
    public static void main(String[] args) throws SyntaxError{

        BalancedTernary[] memf = new BalancedTernary[12];
        memf[0] = new BalancedTernary(7);
        memf[1] = new BalancedTernary(7);
        memf[2] = new BalancedTernary(16);
        memf[3] = new BalancedTernary(0);
        memf[4] = new BalancedTernary(1);
        memf[5] = new BalancedTernary(0);
        memf[6] = new BalancedTernary(1);
        memf[7] = new BalancedTernary(0);
        memf[8] = new BalancedTernary(4);
        memf[9] = new BalancedTernary(1);
        memf[10] = new BalancedTernary(612);
        memf[11] = new BalancedTernary(16);

        String prgrmf = ""
                + "O\n"
                + "o0,1o\n"
                + "+4,0+\n"
                + "+6,1+\n"
                + "x|0|,0x\n"
                + "+3,2,0+\n"
                + "+7,1,3+\n"
                + "*0,7*\n"
                + "*7,0*\n"
                + "+3,1,7+\n"
                + "+0,2,3+\n"
                + "x0,|0|x\n"
                + "+1,6+\n"
                + "+0,4+\n"
                + "o0,1o\n"
                + "O";
        BalancedTernary[] memf2 = new BalancedTernary[3];
        memf2[0] = new BalancedTernary(0);
        memf2[1] = new BalancedTernary(1);
        memf2[2] = new BalancedTernary(2);
        String prgrmf2 = ""
                + "O\n"
                + "+0,1,2+\n"
                + "+0,1,2+\n"
                + "+0,1,2+\n"
                + "O";
        BalancedTernary[] memf3 = new BalancedTernary[2];
        memf3[0] = new BalancedTernary(8);
        memf3[1] = new BalancedTernary(27);
        String prgrmf3 = ""
                + "O\n"
                + "+1,0+\n"
                + "O";

        int num_lines = 1000;
        int mem_size = 500;

        String prgrmf4 = random_program(num_lines, mem_size);
        BalancedTernary[] memf4 = random_mem(mem_size, BalancedTernary.MOD.negate(), BalancedTernary.MOD);
        InstrumentedProcess p = new InstrumentedProcess(prgrmf, memf);
        p.set_verbose(true);

        //System.out.println(p.toString());
        System.out.println();
        p.run();
        System.out.println();
        System.out.println();
        p.run();
        System.out.println();
        //System.out.println(p.toString());
        System.out.println("Reversibility faults: "+p.get_fault_count());
        System.out.println();


    }

}