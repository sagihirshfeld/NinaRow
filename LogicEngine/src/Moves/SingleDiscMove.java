package Moves;

import java.io.Serializable;

public abstract class SingleDiscMove implements Move,Serializable {
    protected final int discNum;
    protected final int selectedCol;

    protected SingleDiscMove(int colToInsert,int discNum){
        selectedCol = colToInsert;
        this.discNum = discNum;
    }

    @Override
    final public int getDiscNum() {
        return discNum;
    }

    final public int getSelectedColumn() {
        return selectedCol;
    }
}
