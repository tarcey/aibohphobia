/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aibohphobia;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
/**
 *
 * @author taw
 */
/*
* This class contains the implementations of all the operators, and as the
* name implies, compiles the source string into an array of functions (as in 
* functional interfaces)
*/
public class Compiler {
    
    /*
    *   To add a new operator:
    *   - add the chosen operator-character to the VALID_OPS string here
    *   - add its forwards-implementation in the static block below
    *
    *   That's it.
    *   Of course, also make sure that your function is actually reversible
    *   character-by-character, and regardless of arguments (what happens if
    *   some or all of the arguments point to the same memory index?)
    */
    private final static String VALID_OPS="+*-iOoxnmbY"; //x, n, m, b are all for swaps
    private final static Function<int[], Function<Memory, int[]>>[] OPS = (Function<int[], Function<Memory, int[]>>[]) new Function[VALID_OPS.length()];

    /*  Program-Counter increments/decrements are implemented as their own function,
    *   but not directly accessible. Instead, they are chained to the appropriate
    *   functions using the .andThen(..) function. Not the most efficient method
    *   (still surprisingly fast though), but easily expandable, and blends in
    *   well without requiring any additional programming work.
    */
    private final static Consumer<Memory> INCR_COUNTER = (mem) ->  {
        mem.increment_program_counter();
    };

    /*
    * Re-orders op arguments for reversal.
     */
    private final static Function<int[], Function<Memory, int[]>> REORDER =  (args) -> {
        return (mem) -> {
            if(mem.get_direction()<0){
                int[] new_args = new int[args.length];
                for(int i=0;i<args.length;i++)
                    new_args[i] = args[args.length-i-1];
                return new_args;
            }
            else{
                int[] new_args = new int[args.length];
                for(int i=0;i<args.length;i++)
                    new_args[i] = args[i];
                return new_args;
            }
        };
    };


    /*
     * Make changes to the op-arguments (in compiled code) persistent.
     */
    private final static Function<int[], BiConsumer<Memory, int[]>> SAVE_ARGS = (args) -> {
        return (mem, new_args) -> {
            if(mem.get_direction()<0)
                for(int i=0;i<args.length;i++)
                    args[i] = new_args[args.length-1-i];
            else
                for(int i=0;i<args.length;i++)
                    args[i] = new_args[i];
        };
    };
    
    
    /*
    * Reversible functions are defined below. Note that this language is
    * reversible character-by-character, not line-by-line.
    * So the reversal of
    *   +1,0,3+
    *   x1,4x
    * is
    *   x4,1x
    *   +3,0,1+
    *
    * Proposal: Use dots instead of commas to seperate indices, because commas
    *  aren't symmetrical in every font, and symmetry is an aesthetic thing to
    *  have in a reversible language.
    */
    
    /*
    *   Looks convoluted (lambda returning a lambda?), but the idea is
    *   that this all happens during compilation, moving overhead away from
    *   runtime. I realize that i'm at the mercy of the JIT here, but one
    *   can be hopeful.
    */
    static{
        
        /*
        *   Addition
        */
        OPS[VALID_OPS.indexOf('+')] = (args) -> {
            switch (args.length) {
                case 2:
                    /*  +A,B+
                    * B' <- A+B
                    * A' <- -B
                    * (if A and B point to the same location, both
                    *  become -A)
                    */
                    return (mem) -> {
                        BalancedTernary reg_B = mem.get(args[1]).negate();
                        mem.set(args[1], mem.get(args[0]).add(mem.get(args[1])));
                        mem.set(args[0], reg_B);
                        return args;
                    };
                case 3:
                    /*  +A,B,C+
                    * A' <- A+B
                    * C' <- C-B
                    * "transfer B from C to A"
                    * This function can be broken down into a reversible sequence
                    * of simpler existing functions.
                    */
                    return (mem) -> {
                        //+A,B+
                        BalancedTernary reg_B_1 = mem.get(args[1]).negate();
                        mem.set(args[1], mem.get(args[0]).add(mem.get(args[1])));
                        mem.set(args[0], reg_B_1);
                        //xA,Bx
                        BalancedTernary reg_A_2 = mem.get(args[0]);
                        mem.set(args[0], mem.get(args[1]));
                        mem.set(args[1], reg_A_2);
                        //+C,B+
                        BalancedTernary reg_B_3 = mem.get(args[1]).negate();
                        mem.set(args[1], mem.get(args[2]).add(mem.get(args[1])));
                        mem.set(args[2], reg_B_3);
                        //xC,Bx
                        BalancedTernary reg_C_4 = mem.get(args[2]);
                        mem.set(args[2], mem.get(args[1]));
                        mem.set(args[1], reg_C_4);
                        return args;
                    };
                default:
                    return null;
            }
        };
        
        /*  Swap
        *
        *   xA,Bx
        *   A' <- B
        *   B' <- A
        */
        OPS[VALID_OPS.indexOf('x')] = (args) -> {
            if(args.length==2)
                return (mem) -> {
                    BalancedTernary swap_temp = mem.get(args[0]);
                    mem.set(args[0], mem.get(args[1]));
                    mem.set(args[1], swap_temp);
                    return args;
                };
            else
                return null;
        };
        /*  Swap
        *
        *   x|A|,|B|x
        *   Indirect swap: The value pointed to by the adress at location A
        *   becomes the value pointed to by the adress at location B, and vice
        *   versa.
        *   Sounds complicated, but is essentially the same thing as array
        *   acces via a variable as in other languages (int a = smth(); int b =
        *   myArr[a];).
        *   This is so far the only function to implicitly modify the code, it
        *   is needed here to store the original location into the line of code
        *   itself, and allow this function to be reversible without needing
        *   "garbage" memory.
        */
        OPS[VALID_OPS.indexOf('n')] = (args) -> {
            if(args.length==2)
                return (mem) -> {
                    int arg_0  = args[0];
                    int arg_1  = args[1];
                    int indirect_arg_0 = mem.roundIndex((int)mem.get(arg_0).longValue());
                    int indirect_arg_1 = mem.roundIndex((int)mem.get(args[1]).longValue());
                    
                    BalancedTernary swap_temp = mem.get(indirect_arg_0);
                    mem.set(indirect_arg_0, mem.get(indirect_arg_1));
                    mem.set(indirect_arg_1, swap_temp);
                    
                    if (arg_0 == indirect_arg_0 || arg_0 == indirect_arg_1){
                        args[0] = arg_0==indirect_arg_0 ? 
                                indirect_arg_1 :
                                indirect_arg_0;
                    }
                    if (arg_1 == indirect_arg_0 || arg_1 == indirect_arg_1){
                        args[1] = arg_1==indirect_arg_1 ? 
                                indirect_arg_0 : 
                                indirect_arg_1;
                    }
                    return args;
                    
                };
            else
                return null;
        };
        /*  Swap
        *
        *   x|A|, Bx
        *   As above, but for the case that only one variable is a pointer.
        */
        OPS[VALID_OPS.indexOf('m')] = (args) -> {
            if(args.length==2)
                return (mem) -> {
                    int arg_0  = args[0];
                    int arg_1  = args[1];
                    int indirect_arg_0;
                    int indirect_arg_1;
                    if(mem.get_direction()>0){
                        indirect_arg_0 = arg_0;
                        indirect_arg_1 = mem.roundIndex((int)mem.get(arg_1).longValue());
                    } else{
                        indirect_arg_0 = mem.roundIndex((int)mem.get(arg_0).longValue());
                        indirect_arg_1 = arg_1;
                    }
                    
                    BalancedTernary swap_temp = mem.get(indirect_arg_0);
                    mem.set(indirect_arg_0, mem.get(indirect_arg_1));
                    mem.set(indirect_arg_1, swap_temp);
                    
                    if(mem.get_direction()<0 && (arg_0 == indirect_arg_0 || arg_0 == indirect_arg_1)){
                        args[0] = arg_0==indirect_arg_0 ? 
                                indirect_arg_1 :
                                indirect_arg_0;
                    }
                    
                    if(mem.get_direction()>0 && (arg_1 == indirect_arg_0 || arg_1 == indirect_arg_1)){
                        args[1] = arg_1==indirect_arg_1 ? 
                                indirect_arg_0 : 
                                indirect_arg_1;
                    }
                        
                    return args;
                };
            else
                return null;
        };
        /*  Swap
        *
        *   xA, |B|x
        *   As above, but for the case that only one variable is a pointer.
        */
        OPS[VALID_OPS.indexOf('b')] = (args) -> {
            if(args.length==2)
                return (mem) -> {
                    int arg_0  = args[0];
                    int arg_1  = args[1];
                    int indirect_arg_0;
                    int indirect_arg_1;
                    if(mem.get_direction()<0){
                        indirect_arg_0 = arg_0;
                        indirect_arg_1 = mem.roundIndex((int)mem.get(arg_1).longValue());
                    } else{
                        indirect_arg_0 = mem.roundIndex((int)mem.get(arg_0).longValue());
                        indirect_arg_1 = arg_1;
                    }
                    
                    BalancedTernary swap_temp = mem.get(indirect_arg_0);
                    mem.set(indirect_arg_0, mem.get(indirect_arg_1));
                    mem.set(indirect_arg_1, swap_temp);
                    
                    if(mem.get_direction()>0 && (arg_0 == indirect_arg_0 || arg_0 == indirect_arg_1)){
                        args[0] = arg_0==indirect_arg_0 ? 
                                indirect_arg_1 :
                                indirect_arg_0;
                    }
                    
                    if(mem.get_direction()<0 && (arg_1 == indirect_arg_0 || arg_1 == indirect_arg_1)){
                        args[1] = arg_1==indirect_arg_1 ? 
                                indirect_arg_0 : 
                                indirect_arg_1;
                    }
                        
                    return args;
                };
            else
                return null;
        };
        
        /*
        *   Multiplication
        */
        OPS[VALID_OPS.indexOf('*')] = (args) -> {
            switch (args.length) {
                case 2:
                    /*  *A,B*
                    * B' <- A*B
                    * A' <- 1/B
                    * (if A and B point to the same location, both
                    *  become 1/B. If A or B have no modular inverse, that is, 
                    *  when at least one is either 0, all 1s, or all -1s, then 
                    *  this is just a swap-operation).
                    */
                    return (mem) -> {
                        BalancedTernary reg_B_1 = mem.get(args[1]);
                        BalancedTernary reg_A_1 = mem.get(args[0]);
                        if(reg_B_1.has_reciprocal() && reg_A_1.has_reciprocal()){
                            reg_B_1 = reg_B_1.reciprocal();
                            reg_A_1 = reg_A_1.multiply(mem.get(args[1]));
                        }
                        mem.set(args[1], reg_A_1);
                        mem.set(args[0], reg_B_1);
                        return args;
                    };
                case 3:
                    /*  *A,B,C*
                    * A' <- A*B
                    * B' <- B
                    * C' <- C/B
                    * This function can be broken down into a reversible sequence
                    * of simpler existing functions.
                    */
                    return (mem) -> {
                        /*
                        *   This would reduce to nothing if any variable had no
                        *   inverse.
                        */
                        if(     mem.get(args[0]).has_reciprocal() && 
                                mem.get(args[1]).has_reciprocal() && 
                                mem.get(args[2]).has_reciprocal()){
                            //*A,B*
                            BalancedTernary reg_B_1 = mem.get(args[1]).reciprocal();
                            mem.set(args[1], mem.get(args[0]).multiply(mem.get(args[1])));
                            mem.set(args[0], reg_B_1);

                            //xA,Bx
                            BalancedTernary reg_A_2 = mem.get(args[0]);
                            mem.set(args[0], mem.get(args[1]));
                            mem.set(args[1], reg_A_2);

                            //*C,B*
                            BalancedTernary reg_B_3 = mem.get(args[1]).reciprocal();
                            mem.set(args[1], mem.get(args[2]).multiply(mem.get(args[1])));
                            mem.set(args[2], reg_B_3);

                            //xC,Bx
                            BalancedTernary reg_C_4 = mem.get(args[2]);
                            mem.set(args[2], mem.get(args[1]));
                            mem.set(args[1], reg_C_4);
                        }
                        return args;
                    };
                default:
                    return null;
            }
        };
        
        /*  Negate
        * 
        *   -A-
        *   A' <- -A
        */
        OPS[VALID_OPS.indexOf('-')] = (args) -> {
            if(args.length==1)
                return (mem) -> {
                    mem.set(args[0], mem.get(args[0]).negate());
                        return args;
                };
            else
                return null;
        };
        
        /*  Multiplicative inverse
        * 
        *   iAi
        *   A' <- 1/A
        *   No-Op when A has no modular inverse, that is, when it is 0, all 1s,
        *   or all -1s.
        */
        OPS[VALID_OPS.indexOf('i')] = (args) -> {
            if(args.length==1)
                return (mem) -> {
                    BalancedTernary reg_A = mem.get(args[0]);
                    if(reg_A.has_reciprocal())
                        reg_A = reg_A.reciprocal();
                    mem.set(args[0], reg_A);
                    return args;
                };
            else
                return null;
        };

        /*  Compare-Negate-Swap
         *
         *   Y A,B Y
         *
         *   What it does:
         *   If A<B, negates A and B, else swaps A and B
         */
        OPS[VALID_OPS.indexOf('Y')] = (args) -> {
            if(args.length==2)
                return (mem) -> {
                    BalancedTernary A = mem.get(args[0]);
                    BalancedTernary B = mem.get(args[1]);
                    if(A.add(B.negate()).longValue() < 0){
                        mem.set(args[0], A.negate());
                        mem.set(args[1], B.negate());
                    } else{
                        mem.set(args[0], B);
                        mem.set(args[1], A);
                    }
                    return args;
                };
            else
                return null;
        };

        /*  Terminate/Reverse
         *
         *   O
         *   Denotes the beginning and end of a program. A program initiates at
         *   O, terminates at O, and is reflected back in the reverse direcion if
         *   the program is resumed at O.
         */
        OPS[VALID_OPS.indexOf('O')] = (args) -> {
            if(args.length==0)
                return (mem) -> {
                    mem.invert_direction();
                    mem.invert_global_direction();
                    return args;
                };
            else
                return null;
        };
        /*  Loop
        *
        *   oA,Bo
        *
        *   What it does:
        *   No-OP if A==B evaluates to TRUE,
        *   MIRROR direction and swap A&B if A==B evaluates to FALSE
        *
        */
        OPS[VALID_OPS.indexOf('o')] = (args) -> {
            if(args.length==2)
                return (mem) -> {
                        BalancedTernary arg0_val = mem.get(args[0]);
                        BalancedTernary arg1_val = mem.get(args[1]);
                        if(arg0_val.compareTo(arg1_val) == 0){
                            //no-op
                        } else{
                            boolean condition;
                            if(mem.get_global_direction() > 0)
                                condition = mem.get_direction() == 0;
                            else
                                condition = mem.get_direction() != 0;

                            if(condition) {
                                mem.set(args[0], arg1_val);
                                mem.set(args[1], arg0_val);
                            }
                            mem.invert_direction();
                        }
                    return args;
                };
            else
                return null;
        };
    }
    
    final static void compile(final SourceCode SRC, final Consumer<Memory>[] FORWARD) throws SyntaxError{
        for(int i=0; i<SRC.length(); i++){
            
            final int OP = VALID_OPS.indexOf(SRC.line(i).d());
            final int[] ARGS = SRC.line(i).t();
            
            if(OP<0)
                throw new SyntaxError("Invalid operation at line "+SRC.get_original_line_index(i)+":\n\t"+SRC.get_original_line(i)+"\n \'"+SRC.line(i).d()+"\' is not a valid operation.");
            
            if(OPS[OP].apply(ARGS)==null)
                throw new SyntaxError("Unexpected number of parameters at line "+SRC.get_original_line_index(i)+":\n\t"+SRC.get_original_line(i));
            
            
            final BiConsumer<Memory, int[]> sa = SAVE_ARGS.apply(ARGS);
            final Function<Memory, int[]> ro = REORDER.apply(ARGS);
            final Consumer<Memory> c1 = (mem) -> sa.accept(mem, OPS[OP].apply(ro.apply(mem)).apply(mem));
            
            FORWARD[i] = c1.andThen(INCR_COUNTER);
        }
    }
}
