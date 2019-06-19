package Program;

import java.util.ArrayList;

enum TreeNodeType{
    TN_TYPE_PROGRAM,
    TN_TYPE_GLOBAL,
    TN_TYPE_FUNC_LIST,
    TN_TYPE_FUNC,
    TN_TYPE_ID,
    TN_TYPE_KEYWORD,
    TN_TYPE_OP_ADD,
    TN_TYPE_OP_SUB,
    TN_TYPE_OP_MUL,
    TN_TYPE_OP_DIV,
    TN_TYPE_ARGUMENTS,
    TN_TYPE_ARGUMENT,
    TN_TYPE_CODE_BLOCK,
    TN_TYPE_STMTS,
    TN_TYPE_STMT_RETURN,
    TN_TYPE_STMT_ASSIGN,
    TN_TYPE_STMT_DECLARE,
    TN_TYPE_EXPR1,
    TN_TYPE_EXPR2,
    TN_TYPE_TERM1,
    TN_TYPE_TERM2,
    TN_TYPE_FACTOR,
    TN_TYPE_FUNCCALL,
    TN_TYPE_EXPRS,
    TN_TYPE_CONST,
    TN_TYPE_UNKNOWN
}

public class TreeNode {
    private TreeNodeType type;
    private ArrayList<TreeNode> subNodes;
    private int TypeTokenIndex = -1;
    private int NameTokenIndex = -1;
    private int TokenIndex = -1;
    public SymbolTable symbolTable;

    public TreeNode() {
        this.type = TreeNodeType.TN_TYPE_UNKNOWN;
        this.subNodes = new ArrayList<TreeNode>();
    }

    public TreeNode(TreeNodeType type) {
        this.type = type;
        this.subNodes = new ArrayList<TreeNode>();
    }

    public TreeNode getSubByIndex(int index) {
        if(index >= this.subNodes.size()) {
            return null;
        }else {
            return this.subNodes.get(index);
        }
    }

    public void setSymbolTable(SymbolTable s) {
        symbolTable = s;
    }

    public TreeNodeType getType() {
        return type;
    }
    public void setType(TreeNodeType type) {
        this.type = type;
    }
    public ArrayList<TreeNode> getSubNodes() {
        return subNodes;
    }
    public void setSubNodes(ArrayList<TreeNode> subNodes) {
        this.subNodes = subNodes;
    }

    public void PrintTree(TreeNode root) {
        PrintTree(root, "");
    }

    private void PrintTree(TreeNode root, String ident) {
        if (root == null)
            return;

        System.out.print(ident);
        System.out.println(root.getType());

        for (TreeNode a : root.getSubNodes()) {
            PrintTree(a, ident + "\t");
        }
    }

    public void setTknIndex(int i, int a) {
        TypeTokenIndex = i;
        NameTokenIndex = a;
    }

    public void setTknIndex(int i) {
        TokenIndex = i;
    }


    public int getNameIndex() {
        return NameTokenIndex;
    }

    public int getTypeIndex() {
        return TypeTokenIndex;
    }

    public int getTokenIndex() {
        return TokenIndex;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
