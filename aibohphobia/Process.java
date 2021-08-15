/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aibohphobia;

import java.util.function.Consumer;

/**
 *
 * @author taw
 */
public class Process {
    
    private final Consumer<Memory>[] PRGRM;
    private final SourceCode SRC;
    private final Memory MEM;
    
    public Process(final String src, final BalancedTernary[] mem) throws SyntaxError{
        this(new SourceCode(src), mem);
    }
    
    public Process(final SourceCode SRC, final BalancedTernary[] mem) throws SyntaxError{
        this.SRC = SRC;
        this.PRGRM = (Consumer<Memory>[]) new Consumer[this.SRC.length()];
        this.MEM = new Memory(mem);
        int[] min_arg = SRC.min_arg();
        int[] max_arg = SRC.max_arg();
        if(min_arg[1]<0){
            throw new SyntaxError("Index out of bounds at line "+SRC.get_original_line_index(min_arg[0])+"\n\t"+SRC.get_original_line(min_arg[0])+"\n Index \'"+min_arg[1]+"\' too low.");
        }
        if(max_arg[1]>=mem.length){
            throw new SyntaxError("Index out of bounds at line "+SRC.get_original_line_index(max_arg[0])+"\n\t"+SRC.get_original_line(max_arg[0])+"\n Index \'"+max_arg[1]+"\' too high.");
        }
        Compiler.compile(this.SRC, this.PRGRM);
    }
    
    
    public final int length(){
        return PRGRM.length;
    }
    
    public final int program_counter(){
        return MEM.next_line();
    }
    
    public void run(){
        boolean term;
        do{
            term = process_line();
        }while(!term);
    }
    
    public int run(int steps){
        if(steps>0){
            boolean term;
            do{
                term = process_line();
                steps--;
            }while(!term && steps>0);
        }
        return steps;
    }
    
    public void reverse_direction(){
        if(MEM.get_direction()==0)
            return;
        MEM.invert_direction();
        MEM.invert_direction();
        MEM.invert_global_direction();
        MEM.increment_program_counter();
    }
    
    boolean process_line(){
        PRGRM[MEM.next_line()].accept(MEM);
        return MEM.get_direction()==0 && (""+SRC.line(MEM.next_line()).d()).equals("O");
    }
    
    public final int mem_size(){
        return MEM.size();
    }
        
    public final Memory memcopy(){
        return MEM.copy();
    }
    
    @Override
    public String toString(){
        return MEM.toString();
    }
}
