/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aibohphobia;

import java.math.BigInteger;

/**
 *
 * @author taw
 */
public class BalancedTernary{
    
    final static int TRITS = 13;
    final static BigInteger MOD = BigInteger.valueOf(3).pow(TRITS).shiftRight(1);
    
    /*  lengths such that the maximum value is coprime with all lower values:
    *   1   -   1
    *   3   -   13
    *   7   -   1093
    *   13  -   797161
    *   71  -   3754733257489862401973357979128773
    *   103 -   6957596529882152968992225251835887181478451547013
    *   541 -   663084395471818436731691851499754503355465637904740797172567784209623516341640238400510288503463008441736852119221000000301102907948169398408014645814376158251490141606616528088759064995410612765793960501606910585490086339893058591064091241255832207903808201
    *   1091 -  ...
    *   1367 -  ...
    *   1627 -   
    *   4177 -    
    *   9011 -   
    */
    
    //little-endian: The smallest digit is at index 0
    final static byte P  = 1;
    final static byte Z = 0;
    final static byte N  = -1;
    private final byte[] NUMBER;
    
    
    public long[] balance(){
        long n=0, p=0;
        for(byte b: NUMBER){
            if(b==N) n+=1;
            else if(b==P) p+=1;
        }
        return new long[]{n, p};
    }
    
    public boolean has_reciprocal(){
        long[] balance = this.balance();
        if(balance[0]==0 && balance[1]==0)
            return false;
        else return !(balance[0] == this.NUMBER.length || balance[1] == this.NUMBER.length);
    }
    
    public BalancedTernary reciprocal(){
        BigInteger a = this.toBigInteger();
        BigInteger out = a.modInverse(MOD);
        if(a.signum()<0)
            out = out.subtract(MOD);
        return new BalancedTernary(out);
    }
    
    public final long max_value(){
        return MOD.longValue();
    }
    
    public static int mod(int a, int m){
        return ((a%m)+m)%m;
    }
    
    public BalancedTernary(){
        NUMBER = new byte[TRITS];
    }
    
    public BalancedTernary(final long NUM){
        NUMBER = fromLong(TRITS, NUM);
    }
    
    public BalancedTernary(final BigInteger NUM){
        NUMBER = fromBigInteger(TRITS, NUM);
    }
    
    private BalancedTernary(final byte[] NUMBER){
        this.NUMBER = NUMBER;
    }
    
    public String stringTrits(){
        String out = "";
        for(byte b: NUMBER)
            out += b==Z?" 0 ":b==P?"+1 ":"-1 ";
        return out;
    }
    
    public BalancedTernary negate(){
        return new BalancedTernary(negate(NUMBER));
    }
    
    public BalancedTernary shift(int amount){
        return new BalancedTernary(shift(this.NUMBER, amount));
    }
    
    public BalancedTernary add(final BalancedTernary other){
        return new BalancedTernary(add(this.NUMBER, other.NUMBER));
    }
    
    public boolean equal(final BalancedTernary other){
        if(this.NUMBER.length != other.NUMBER.length)
            return false;
        for(int i=0;i<this.NUMBER.length;i++)
            if(this.NUMBER[i] != other.NUMBER[i])
                return false;
        return true;
    }
    
    private final BigInteger modNeg(BigInteger a){
        return modNeg(a, MOD);
    }
    
    private final static BigInteger modNeg(BigInteger a, final BigInteger MOD){
        int sig = a.signum();
        a = a.mod(MOD);
        if(sig<0)
            a = (MOD.subtract(a)).negate();
        return a;
    }
    
    public BalancedTernary multiply(final BalancedTernary other){
        
        BigInteger th = modNeg(this.toBigInteger());
        BigInteger result = th.multiply(modNeg(other.toBigInteger()));
        
        return new BalancedTernary(modNeg(result));
    }
    
    public long longValue(){
        return toLong(this.NUMBER);
    }
    
    public BigInteger toBigInteger(){
        return toBigInteger(this.NUMBER);
    }
    
    @Override
    public String toString(){
        return toBigInteger().toString();
    }
    
    private static byte[] add(byte a, byte b){
        if(a==Z || b==Z){
            return new byte[]{(byte)(a|b), Z};
        } else if(a==P){
            if(b==P)
                return new byte[]{N,P};
            else return new byte[]{Z,Z};
        } else{
            if(b==P)
                return new byte[]{Z,Z};
            else
                return new byte[]{P,N};
        }
    }
    
    private static byte[] add(byte a, byte b, byte carry){
        byte[] i1 = add(a, b);
        byte[] i2 = add(i1[0], carry);
        i2[1] = add(i1[1], i2[1])[0];
        return i2;
    }
    
    public static byte[] add(byte[] a, byte[] b){
        byte[] out = new byte[a.length];
        byte carry = Z;
        for(int i=0;i<a.length;i++){
            byte[] c = add(a[i], b[i], carry);
            out[i] = c[0];
            carry = c[1];
        }
        //if(carry != Z)
        //    return add(out, carry);
        return out;
    }
    
    public static byte[] add2(byte[] a, byte[] b){
        byte[] out = new byte[a.length];
        for(int i=0;i<a.length;i++){
            byte[] c = add(a[i], b[i]);
            out[i] = c[0];
        }
        //if(carry != Z)
        //    return add(out, carry);
        return out;
    }
    
    public static byte[] add(byte[] a, byte carry){
        byte[] out = new byte[a.length];
        for(int i=0;i<a.length;i++){
            byte[] c = add(a[i], carry);
            out[i] = c[0];
            carry = c[1];
        }
        if(carry != Z)
            return add(out, carry);
        return out;
    }
    
    public int signum(){
        return signum(this.NUMBER);
    }
    
    public int compareTo(final BalancedTernary other){
        long a= this.longValue();
        long b= other.longValue();
        if(a<b)
            return -1;
        if(a==b)
            return 0;
        return 1;
        //return this.add(other.negate()).signum();
    }
    
    public static int signum(byte[] a){
        int i=a.length-1;
        for(;i>0;i--){
            if(a[i]!=Z)
                break;
        }
        return a[i]==Z?0:a[i]==P?1:-1;
    }
    
    public static byte[] shift(byte[] in, int shift){
        byte[] out = new byte[in.length];
        if(shift>=0)
            System.arraycopy(in, 0, out, shift, out.length-shift);
        else
            System.arraycopy(in, -shift, out, 0, out.length+shift);
        return out;
    }
    
    public static byte[] negate(byte[] in){
        byte[] out = new byte[in.length];
        for(int i=0;i<in.length;i++){
            out[i] = in[i]==P?N:in[i]==N?P:Z;
        }
        return out;
    }
    
    public static long toLong(byte[] number){
        long out = 0;
        long place = 1;
        for(int i=0;i<number.length;i++){
            out += number[i]*place;
            place *= 3;
        }
        return out;
    }
    
    public static BigInteger toBigInteger(byte[] number){
        BigInteger out = BigInteger.ZERO;
        BigInteger place = BigInteger.ONE;
        BigInteger three = BigInteger.valueOf(3);
        for(int i=0;i<number.length;i++){
            out = out.add(place.multiply(BigInteger.valueOf(number[i])));
            place = place.multiply(three);
        }
        return out;
    }
    
    public static byte[] fromLong(final int TRITS, long in){
        byte[] out = new byte[TRITS];
        byte[] twoPow = new byte[TRITS];
        boolean neg = false;
        if(in<0){
            in *= -1;
            neg = true;
        }
        twoPow[0] = P;
        while(in>0){
            if((in&1) > 0){
                out = add(out, twoPow);
            }
            in >>>=1;
            twoPow = add(twoPow, twoPow);
        }
        if(neg)
            out = negate(out);
        return out;
    }
    
    public static byte[] fromBigInteger(final int TRITS, BigInteger in){
        byte[] out = new byte[TRITS];
        byte[] twoPow = new byte[TRITS];
        boolean neg = false;
        if(in.signum()<0){
            in = in.negate();
            neg = true;
        }
        twoPow[0] = P;
        while(in.signum()!=0){
            if(in.testBit(0)){
                out = add(out, twoPow);
            }
            in = in.shiftRight(1);
            twoPow = add(twoPow, twoPow);
        }
        if(neg)
            out = negate(out);
        return out;
    }
}
