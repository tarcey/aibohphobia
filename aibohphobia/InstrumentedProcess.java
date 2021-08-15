/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aibohphobia;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author taw
 */
/*
* This class is used to run Programs in a more controllable
* and checked fashion, as opposed to the bare-bones Process class.
*/
public class InstrumentedProcess extends Process {
        
    private boolean verbose = false;
    
    private final ArrayList<int[]> timeline = new ArrayList<>();
    private int fault_count=0;
    private int global_fw_steps = -1;
    private int global_direction = 1;
    
    public InstrumentedProcess(final String SRC, final BalancedTernary[] MEM) throws SyntaxError{
        super(SRC, MEM);
    }
    
    public InstrumentedProcess(final SourceCode SRC, final BalancedTernary[] MEM) throws SyntaxError{
        super(SRC, MEM);     
    }
    
    public void set_verbose(final boolean VAL){
        verbose = VAL;
    }
    
    public int get_fault_count(){
        return fault_count;
    }
    
    private boolean checkOrAddState(){
        boolean reversible = false;

        if(global_direction>0)
            global_fw_steps = global_fw_steps+global_direction;

        int cntr = global_fw_steps;
        if(timeline.size()<=cntr){
            timeline.add(dump());
            reversible = true;
        }
        else{
            int[] stateHash = dump();
            reversible = Arrays.equals(stateHash, timeline.get(cntr));
            if(reversible==false)
                timeline.set(cntr, stateHash);
        }
        
        if(!reversible){
            if(verbose) System.out.println(super.toString()+"\tGC:"+cntr+"\t*");
            fault_count++;
        } else if(verbose) System.out.println(super.toString()+"\tGC:"+cntr);

        if(global_direction<0)
            global_fw_steps = global_fw_steps+global_direction;

        return reversible;
    }
    
    @Override
    public void run(){
        final int num_faults = fault_count;
        
        checkOrAddState();
        boolean term;
        do{
            term = process_line();
            checkOrAddState();
        }while(!term);
        global_direction *= -1;
        
        if(fault_count!=num_faults)
            System.out.println("Reversible: NO!\n\tFaults: "+(fault_count-num_faults)+"\n");
        else if(verbose) System.out.println("Reversible: YES!\n");
    }
    
    @Override
    public int run(int steps){
        
        
        final int num_faults = fault_count;
        
        if(steps>0){
            checkOrAddState();
            boolean term;
            do{
                term = process_line();
                checkOrAddState();
                steps--;
            }while(!term && steps>0);
            if(term)
                global_direction *= -1;
        }
        
        if(fault_count!=num_faults)
            System.out.println("Reversible: NO!\n\tFaults: "+(fault_count-num_faults)+"\n");
        else if(verbose) System.out.println("Reversible: YES!\n");
        
        return steps;
    }
    
    private int[] dump(){
        int memsize = mem_size();
        int[] out = new int[memsize*2 + 1];
        Memory mem = super.memcopy();
        for(int i=0;i<memsize;i++){
            out[i] = mem.get(i).toBigInteger().hashCode();
            out[i+memsize] = mem.get(i).toBigInteger().hashCode();
        }
        out[out.length-1] = mem.last_line();
        return out;
    }
}
