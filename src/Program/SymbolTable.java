package Program;

import java.util.ArrayList;

class Symbol {
    private String name;
    private String type;
    private int addr;
    private int offset;
    private int reg;

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
}
