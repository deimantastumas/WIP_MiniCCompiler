package Program;

import java.util.ArrayList;

class Symbol {
    private String name;
    private String type;
    private int addr;
    private int offset;
    private int reg;

    private int valUsed = 0;

    public Symbol(String name, String type, int addr, int offset, int reg) {
        this.name = name;
        this.type = type;
        this.addr = addr;
        this.offset = offset;
        this.reg = reg;
    }

    public String GetName() {
        return name;
    }

    public String GetType() {
        return type;
    }

    public int getAddr() {
        return addr;
    }

    public int getOffset() {
        return offset;
    }

    public int getReg() {
        return reg;
    }

    public void setReg(int reg) {
        this.reg = reg;
    }

    public void setValUsed() {
        valUsed = 1;
    }

    public int getValUsed() {
        return valUsed;
    }
}

public class SymbolTable {
    public ArrayList<Symbol> symbols;
    private SymbolTable upperTable = null;

    public SymbolTable() {
        this.symbols = new ArrayList<>();
    }

    public void insert(Symbol s) {
        symbols.add(s);
    }

    public Symbol findByName(String name) {
        for (Symbol a : symbols) {
            if (a.GetName().equals(name))
                return a;
        }
        return null;
    }

    public void setUpperTable(SymbolTable table) {
        upperTable = table;
    }

    public SymbolTable getUpperTable() {
        return upperTable;
    }

    @Override
    public String toString() {
        String table = "";
        for (Symbol a : symbols) {
            table += a.GetName() + "\t" + a.GetType() + "\t" + a.getAddr() + "\t" + a.getOffset() + "\t" + a.getReg() + "\n";
        }
        return table;
    }
}