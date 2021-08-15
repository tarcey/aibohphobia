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
class SyntaxError extends Exception{
    public SyntaxError(String message){
        super(message);
    }
}
/*
* I'm hoping to never need this exception. I want reversibility to arise from
* correct syntax.
*/
//class ReversibilityError extends Exception{
//    public ReversibilityError(String message){
//        super(message);
//    }
//    public ReversibilityError(){
//        super();
//    }
//}