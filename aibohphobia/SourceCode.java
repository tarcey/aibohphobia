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
*   Parses the source for compilation, by converting it from the source
*   string into an intermediate form, while also taking care of some things that
*   make more sense here than in the compiler (source comments, basic syntax, etc). 
*   Keeps track of how the indices of "functional lines" relate to lines in the
*   original source (e.g. there would be a mismatch after removing comments and
*   empty lines), to provide better info in the case of errors.
*
*   Instances are discarded after successful compilation.
*
*   TODO: This should be useful to keep for debugging programs
*
*/
public class SourceCode {
    
    private final String SRC;
    private final Pair<Character, int[]>[] LINES;
    private final int[] LINE_NUMBERS;
    private final String[] ORIG_LINES;
    
    //Converts an index used by the compiler into the according index in the
    //original source
    public int get_original_line_index(final int functional_line_index){
        return LINE_NUMBERS[functional_line_index];
    }
    public String get_original_line(final int functional_line_index){
        return ORIG_LINES[functional_line_index];
    }
    
    //Maximum/Minimum arguments to check wether they stay in the range of
    //allocated memory.
    public int[] max_arg(){
        int max_arg=0;
        int max_arg_index=0;
        for(int i=0;i<LINES.length;i++){
            for(int j=0;j<LINES[i].t().length;j++)
                if(LINES[i].t()[j] > max_arg){
                    max_arg = LINES[i].t()[j];
                    max_arg_index=i;
                }
        }
        return new int[]{max_arg_index, max_arg};
    }    
    public int[] min_arg(){
        int min_arg=Integer.MAX_VALUE;
        int min_arg_index=0;
        for(int i=0;i<LINES.length;i++){
            for(int j=0;j<LINES[i].t().length;j++)
                if(LINES[i].t()[j] < min_arg){
                    min_arg = LINES[i].t()[j];
                    min_arg_index=i;
                }
        }
        return new int[]{min_arg_index, min_arg};
    }
    
    //This is what the compiler works with, a pair consisting of the operator-
    //character, and its arguments.
    public Pair<Character, int[]> line(final int functional_line_index){
        return LINES[functional_line_index];
    }
    
    public String get_source_code(){
        return SRC;
    }
    
    //The number of functional lines in the source code.
    public int length(){
        return LINES.length;
    }
    
    public SourceCode(final String SRC) throws SyntaxError{
        this.SRC = SRC;
        
        String[] all_lines = this.SRC.trim().split("\\r?\\n");
        
        int counter=0;
        LINES = (Pair<Character, int[]>[]) new Pair[all_lines.length];
        LINE_NUMBERS = new int[all_lines.length];
        ORIG_LINES = new String[all_lines.length];
        
        //Do processing
        
        //Stack<Character> par_check  = new Stack<>();
        for(int i=0;i<all_lines.length;i++){
            Pair<Character, int[]> line = process_line(all_lines[i].replaceAll("\\s+",""), all_lines[i], i);
            if(line!=null){
                if(counter==0 && line.d()!='O')
                    throw new SyntaxError("Illegal operation at line "+i+":\n\t"+all_lines[i]+"\nTermination/Reversal-character \'O\' must be the first operation in a program.");
        
                LINES[counter]=line;
                LINE_NUMBERS[counter]=i;
                ORIG_LINES[counter]=all_lines[i];
                counter++;
                //There are no parantheses in this language (yet).
                //if(!check_parenthesis_balance(par_check, line.getKey()))
                //    throw new SyntaxError("Unbalanced Parentheses. Unexpected \'"+line.getKey()+"\' at line "+i+":\n\t"+all_lines[i]);
            }
        }
        //if(!par_check.empty())
        //    throw new SyntaxError("Unbalanced Parentheses. Reached end of string searching for \'"+par_check.peek()+"\'");
    }
    
    private static Pair<Character, int[]> process_line(final String L, final String original_line, final int L_NUM) throws SyntaxError{
        if(L.charAt(0)=='#')
            return null;
        char f = L.charAt(0);
        
        if(f=='O'){
            if(L.length()>1)
                throw new SyntaxError("Malformed expression at line "+L_NUM+":\n\t"+original_line+"\nTermination-character \'"+f+"\' must be the only character in its line.");
            return new Pair(f, new int[]{});
        }
        
        if(L.charAt(L.length()-1) != f)
            throw new SyntaxError("Malformed expression at line "+L_NUM+":\n\t"+original_line+"\nFunction-character \'"+f+"\' must appear at the beginning and end of the line.");
        
        String arg_string = L.substring(1, L.length()-1);
        String arg_strings[] = arg_string.split(",");
        
        //Handle dereferencing in swaps
        if(arg_string.contains("|")){
            if(f != 'x')
                throw new SyntaxError("Malformed expression at line "+L_NUM+":\n\t"+original_line+"\nDereferencing via \'|\' is only allowed for the operands of the swap \'x\' operation.");
            else{
                if(arg_strings.length!=2)
                    throw new SyntaxError("Unexpected number of parameters at line "+L_NUM+":\n\t"+original_line);
                if(arg_strings[0].contains("|")){
                    if(!(arg_strings[0].charAt(0)=='|' && arg_strings[0].charAt(arg_strings[0].length()-1)=='|'))
                        throw new SyntaxError("Malformed expression at line "+L_NUM+":\n\t"+original_line+"\n");
                    arg_strings[0] = arg_strings[0].replace('|', ' ').trim();
                    f = 'b';
                }
                if(arg_strings[1].contains("|")){
                    if(!(arg_strings[1].charAt(0)=='|' && arg_strings[1].charAt(arg_strings[1].length()-1)=='|'))
                        throw new SyntaxError("Malformed expression at line "+L_NUM+":\n\t"+original_line+"\n");
                    arg_strings[1] = arg_strings[1].replace('|', ' ').trim();
                    f = f=='x'?'m':'n';
                }
                
            }
        }
        
        
        int[] args = new int[arg_strings.length];
        if(arg_string.length()>0)
            for(int i=0; i<args.length; i++){
                try{
                    args[i] = Integer.parseInt(arg_strings[i]);
                } catch(NumberFormatException e){
                    throw new SyntaxError("Parse error at line "+L_NUM+":\n\t"+original_line+"\"\nParameter \""+arg_strings[i]+"\" is not an integer.");
                }
            }
        return new Pair(f, args);
    }
    
    //private static boolean check_parenthesis_balance(Stack<Character> stack, char c){
    //    switch (c) {
    //        case '{':
    //        case '(':
    //        case '[':
    //            stack.push(c);
    //            return true;
    //        case '}':
    //            return !stack.isEmpty() && stack.pop() == '{';
    //        case ')':
    //            return !stack.isEmpty() && stack.pop() == '(';
    //        case ']':
    //            return !stack.isEmpty() && stack.pop() == '[';
    //        default:
    //            return true;
    //    }
    //}
}
