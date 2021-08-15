package aibohphobia;

class Pair<A, B>{
    private final A AA;
    private final B BB;

    Pair(final A AA, final B BB){
        this.AA = AA;
        this.BB = BB;
    }
    final A d(){
        return AA;
    }
    final B t(){
        return BB;
    }
}
