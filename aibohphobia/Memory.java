/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aibohphobia;

/**
 *
 * @author taw
 */
/*
* Takes care of the usable memory (consists of whole ternary numbers, does not
* include the program itself) and the program counter.
*/
public class Memory {
    
    private final BalancedTernary[] MEM;
    private int PRGRM_COUNTER = 0;
    private int GLOBAL_DIRECTION = 2;
    private int direction = 2; //1: forward, -1: backwards, 2 := -i, 3=i
                               //2 and 3 (-i, i) serve to implement reflection
                               //at Beginning/End cleanly

    public static String myTab(int currentLength, int width, int num){
        String out = "";
        if(num>0){
            int up = ((currentLength + width - 1) / width) * width;
            if(up==currentLength) up += width;
            for(int i=currentLength;i<up;i++)
                out += " ";
            num--;
            while(num>0){
                for(int i=0;i<width;i++)
                    out += " ";
                num--;
            }
        }
        return out;
    }
    @Override
    public String toString(){
        String out = "MEM[";
        for(int i=0;i<MEM.length-1;i++){
            out += MEM[i]+",";
            out += myTab(out.length(),5,1);
        }
        if(MEM.length>0)
            out += MEM[MEM.length-1]+"]";
        else 
            out += "]";
        
        out += myTab(out.length(), 4, 3)+"CN: "+this.last_line();
        long[] balance = balance();
        out += myTab(out.length(), 4, 1)+"BALANCE: n:"+balance[0];
        out += myTab(out.length(), 4, 1)+"p:"+balance[1];
        out += myTab(out.length(), 4, 1)+"n+p:"+(balance[0]+balance[1]);
        
        return out;
    }
    
    public long[] balance(){
        long n=0, p=0;
        for(BalancedTernary t: MEM){
            long[] b = t.balance();
            n += b[0];
            p += b[1];
        }
        return new long[]{n, p};
    }
    
    public Memory(final BalancedTernary[] MEM){
        this.MEM = MEM;
    }
    
    private Memory(final Memory M){
        this.MEM = M.MEM;
        this.PRGRM_COUNTER = M.PRGRM_COUNTER;
        this.direction = M.direction;
    }
    
    public int roundIndex(int index){
        index = index % MEM.length;
        if(index<0) index += MEM.length;
        return index;
    }
    
    public BalancedTernary get(int index){
        return MEM[index];
    }
    
    void set(int index, BalancedTernary val){
        MEM[index] = val;
    }
    
    void increment_program_counter(){
        PRGRM_COUNTER += this.get_direction();
    }

    void jump(final int NEXT_POSITION){
        PRGRM_COUNTER = NEXT_POSITION-this.get_direction();
    }
    
    public BalancedTernary[] data(){
        return MEM;
    }
    
    void invert_direction(){
        switch(direction){
            case -1:
                direction = 2;
                break;
            case 2:
                direction = 1;
                break;
            case 1:
                direction = 3;
                break;
            case 3:
                direction = -1;
                break;
            default:
                break;              
        }
    }
    
    public Memory copy(){
        return new Memory(this);
    }
    
    public int size(){
        return MEM.length;
    }

    void invert_global_direction() {
        switch (GLOBAL_DIRECTION) {
            case -1:
                GLOBAL_DIRECTION = 2;
                break;
            case 2:
                GLOBAL_DIRECTION = 1;
                break;
            case 1:
                GLOBAL_DIRECTION = 3;
                break;
            case 3:
                GLOBAL_DIRECTION = -1;
                break;
            default:
                break;
        }
    }

    public int get_direction(){
        if(direction==2 || direction==3)
            return 0;
        else
            return direction;
    }

    public int get_global_direction(){
        if(GLOBAL_DIRECTION==2 || GLOBAL_DIRECTION==3)
            return 1;
        else if (GLOBAL_DIRECTION==3)
            return -1;
        else
            return GLOBAL_DIRECTION;
    }
    
    public int next_line(){
        return PRGRM_COUNTER;
    }

    public int last_line(){
        return PRGRM_COUNTER+(direction<0?1:0);
    }
}
