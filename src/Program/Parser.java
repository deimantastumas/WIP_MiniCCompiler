package Program;

/*
    Program     -> func_list
    func_list   -> func func_list | e
    func        -> type id '(' arguments ')' code_block
    type        -> int | float | char
    arguments   -> e | arg_list
    arg_list    -> argument ',' arg_list | argument
    argument    -> type id
    code_block  -> '{' stmts '}'
    stmts       -> stmt stmts | e
    stmt        -> assign_stmt | return_stmt | decl_stmt | func_call
    assign_stmt -> id '=' expr ';'
    return_stmt -> return expr ';'
    decl_stmt   -> type id ';'

    expr        -> expr '+' term | expr '-' term | term  (can't use top down parsing, so: )

    expr        -> term expr'
    expr'       -> '+' term expr' | '-' term expr' | e

    term        -> factor term'
    term'       -> '*' factor term' | '/' factor term' | e (+, -, ;)

    factor      -> '(' expr ')' | id | const | func_call
    func_call   -> id '(' expr_list ')' ';'
    expr_list   -> expr | expr ',' expr_list | e

    program         -> func_list
                    -> func func_list
                    -> type id '(' arguments ')' code_block func_list
                    -> int id '(' arguments ')' code_block func_list
                    -> int id '(' arg_list ')' code_block func_list
                    -> int id '(' argument ',' arg_list ')' code_block func_list
                    -> int id '(' type id ',' arg_list ')' code_block func_list
                    -> int id '(' int id ',' arg_list ')' code_block func_list
                    -> int id '(' int id ',' argument ')' code_block func_list
                    -> int id '(' int id ',' type id ')' code_block func_list
                    -> int id '(' int id ',' int id ')' code_block func_list
                    -> int id '(' int id ',' int id ')' '{' stmts '}' func_list
                    -> int id '(' int id ',' int id ')' '{' stmt stmts '}' func_list
                    -> int id '(' int id ',' int id ')' '{' return_stmt stmts '}' func_list
                    -> int id '(' int id ',' int id ')' '{' return expr ';' stmts '}' func_list
                    -> int id '(' int id ',' int id ')' '{' return term expr' ';' stmts '}' func_list
 */

import java.util.HashSet;

class Parser {
    private static Scanner.TKN_TYPE nextToken;
    private static int tokenIndex = 0;
    private static TreeNode root;
    private static String currentFunc;
    static void run() {
        System.out.println("\nParse tree after parsing:\n");

        root = program();                        //Build unbalanced parse tree
        BalanceTree(root);                       //Balance parse tree
        root.PrintTree(root);                    //Print balanced parse tree
        buildTable(root, new SymbolTable());     //Build symbol tables
        System.out.println("\nSymbol tables:\n");
        printTables(root);                       //Print symbol tables
        dupCheck(root, new SymbolTable());       //Check for duplicate functions or vars
        varCheck(root, root);                    //Check if variables are initialized
    }

    public static TreeNode getRoot() {
        return root;
    }

    public static String getLexeme(int index) {
        if (index < Scanner.getTokenLexeme().size())
            return Scanner.getTokenLexeme().get(index);
        return "";
    }

    private static void BalanceTree(TreeNode all) {
        SyntaxToAst(all);
        int funcCount = all.getSubNodes().size();
        for (int i = 0; i < funcCount; i++) {
            for (TreeNode a : all.getSubByIndex(i).getSubNodes()) {
                if (a.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK) {
                    SyntaxToAst(a); // code_block
                    // a -> we are inside code_block
                    for (TreeNode cBlockElm : a.getSubNodes()) {
                        //we go through various statements inside codeblock
                        for (TreeNode b : cBlockElm.getSubNodes()) {
                            // we go through various things inside statements
                            if (b.getType() == TreeNodeType.TN_TYPE_EXPRS) {
                                SyntaxToAst(b);
                                for (TreeNode expr1 : b.getSubNodes())
                                    SyntaxToAst(expr1);
                            }

                            if (b.getType() == TreeNodeType.TN_TYPE_EXPR1)
                                SyntaxToAst(b);
                        }
                    }
                }
                if (a.getType() == TreeNodeType.TN_TYPE_ARGUMENTS)
                    SyntaxToAst(a); // arguments
            }
        }
    }

    private static void printTables(TreeNode root) {
        if (root.getType() == TreeNodeType.TN_TYPE_PROGRAM) {
            System.out.println("Global functions:");
            for (Symbol a : root.symbolTable.symbols) {
                System.out.println(a.GetName() + " " + a.GetType());
            }
            System.out.println();
        }

        if (root.getType() == TreeNodeType.TN_TYPE_FUNC) {
            String funcName = getLexeme(root.getNameIndex());
            currentFunc = funcName;
            for (TreeNode node : root.getSubNodes()) {
                if (node.getType() == TreeNodeType.TN_TYPE_ARGUMENTS) {
                    System.out.println(funcName + " function arguments:");
                    for (Symbol a : node.symbolTable.symbols) {
                        System.out.println(a.GetName() + " " + a.GetType());
                    }
                    System.out.println();
                }
            }
        }

        if (root.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK) {
            if (root.symbolTable.symbols.size() > 0) {
                System.out.println(currentFunc + " function variable declarations:");
                for (Symbol a : root.symbolTable.symbols) {
                    System.out.println(a.GetName() + " " + a.GetType());
                }
                System.out.println();
            }
        }

        for (TreeNode a : root.getSubNodes()) {
            printTables(a);
        }
    }

    private static void SyntaxToAst(TreeNode root) {
        if (root.getType() == TreeNodeType.TN_TYPE_EXPRS) {
            if (root.getSubNodes().size() >= 1) {
                TreeNode exprs = root;
                if (exprs.getSubNodes().size() == 1)
                    return;
                int i = 1;
                while (true) {
                    exprs = exprs.getSubByIndex(1);
                    TreeNode expr1 = exprs.getSubByIndex(0);
                    root.getSubNodes().add(i++, expr1);
                    if (exprs.getSubNodes().size() == 1)
                        break;
                }
                root.getSubNodes().remove(root.getSubNodes().size()-1);
            }
        }

        if (root.getType() == TreeNodeType.TN_TYPE_PROGRAM
                || root.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK
                || root.getType() == TreeNodeType.TN_TYPE_ARGUMENTS) {
            if (root.getSubNodes().size() >= 1) {
                TreeNode fl = root.getSubByIndex(0);

                int i = 0;
                while (true) {
                    if (fl.getSubNodes().size() == 0)
                        break;
                    TreeNode f = fl.getSubByIndex(0);
                    root.getSubNodes().add(i++, f);
                    if (fl.getSubNodes().size() >= 2) {
                        fl = fl.getSubByIndex(1);
                    }
                    else break;
                }
            }
            if (root.getSubNodes().size() == 0)
                System.out.println("ERROR: Code doesn't follow grammar");
            else
                root.getSubNodes().remove(root.getSubNodes().size()-1);
        }

        if (root.getType() == TreeNodeType.TN_TYPE_TERM1) {
            TreeNode term2 = root.getSubByIndex(1);

            while (true) {
                if (term2.getSubNodes().size() == 0)
                    break;
                TreeNode opNode = term2.getSubByIndex(0);
                TreeNode factorNode = term2.getSubByIndex(1);

                if (factorNode.getSubByIndex(0).getType() == TreeNodeType.TN_TYPE_EXPR1) {
                    SyntaxToAst(factorNode.getSubByIndex(0));
                }

                root.getSubNodes().add(opNode);
                root.getSubNodes().add(factorNode);

                if (term2.getSubNodes().size() >= 3) {
                    term2 = term2.getSubByIndex(2);
                }
                else break;
            }
            if (root.getSubNodes().size() >= 2)
                root.getSubNodes().remove(1);
        }

        if (root.getType() == TreeNodeType.TN_TYPE_EXPR1) {
            if (root.getSubNodes().size() > 1) {
                TreeNode term = root.getSubByIndex(0);
                TreeNode factorInsideTerm = term.getSubByIndex(0);
                if (factorInsideTerm.getSubByIndex(0).getType() == TreeNodeType.TN_TYPE_EXPR1)
                    SyntaxToAst(factorInsideTerm.getSubByIndex(0));

                if (term.getSubNodes().size() >= 2)
                    SyntaxToAst(term);

                TreeNode exp2 = root.getSubByIndex(1);
                while (true) {
                    if (exp2.getSubNodes().size() == 0)
                        break;
                    TreeNode opNode = exp2.getSubByIndex(0);
                    TreeNode tmNode = exp2.getSubByIndex(1);

                    if (tmNode.getSubNodes().size() >= 2) {
                        SyntaxToAst(tmNode);
                    }

                    TreeNode fac = tmNode.getSubByIndex(0);
                    if (fac.getSubByIndex(0).getType() == TreeNodeType.TN_TYPE_EXPR1) {
                        SyntaxToAst(fac.getSubByIndex(0));
                    }

                    root.getSubNodes().add(opNode);
                    root.getSubNodes().add(tmNode);

                    if (exp2.getSubNodes().size() >= 3) {
                        exp2 = exp2.getSubByIndex(2);
                    }
                    else break;
                }
            }
            if (root.getSubNodes().size() >= 2)
                root.getSubNodes().remove(1);

            FurtherBalance(root);
        }
    }

    private static void FurtherBalance(TreeNode root) {
        if (root.getType() == TreeNodeType.TN_TYPE_EXPR1) {
            if (root.getSubNodes().size() == 3) {
                TreeNode cnst = root.getSubByIndex(0).getSubByIndex(0).getSubByIndex(0);
                root.getSubByIndex(0).getSubNodes().add(0, cnst);
                root.getSubByIndex(0).getSubNodes().remove(1);

                cnst = root.getSubByIndex(2).getSubByIndex(0).getSubByIndex(0);
                root.getSubByIndex(2).getSubNodes().add(0, cnst);
                root.getSubByIndex(2).getSubNodes().remove(1);
            }
            else if (root.getSubNodes().size() == 1) {
                TreeNode cnst = root.getSubByIndex(0).getSubByIndex(0).getSubByIndex(0);
                root.getSubNodes().add(0, cnst);
                root.getSubNodes().remove(1);

                if (root.getSubByIndex(0).getType() == TreeNodeType.TN_TYPE_FUNCCALL)
                    SyntaxToAst(root.getSubByIndex(0).getSubByIndex(1));
            }
            else
                System.out.println("ERROR: EXPR1 INCORRECT");
        }
    }

    private static void varCheck(TreeNode root, TreeNode currentParent) {
        if (root.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK) {
            currentParent = root;
            for (TreeNode node : root.getSubNodes()) {
                if (node.getType() == TreeNodeType.TN_TYPE_STMT_ASSIGN) {
                    String name = getLexeme(node.getTokenIndex());
                    boolean initialized = SearchScopes(root, name);
                    if (!initialized) {
                        System.out.println("Initialization error: " + name + " is not initialized!");
                    }
                }
            }
        }

        if (root.getType() == TreeNodeType.TN_TYPE_ID) {
            if (root.getTokenIndex() != -1) {
                String name = getLexeme(root.getTokenIndex());
                boolean initialized = SearchScopes(currentParent, name);
                if (!initialized) {
                    System.out.println("Initialization error: " + name + " is not initialized!");
                }
            }
        }

        for (TreeNode a : root.getSubNodes()) {
            varCheck(a, currentParent);
        }
    }

    private static boolean SearchScopes(TreeNode root, String name) {
        SymbolTable rootTable = root.symbolTable;
        while (rootTable != null) {
            for (Symbol a : rootTable.symbols) {
                if (a.GetName().equals(name))
                    return true;
            }
            rootTable = rootTable.getUpperTable();
        }
        return false;
    }

    private static void dupCheck(TreeNode root, SymbolTable st) {
        if (root.getType() == TreeNodeType.TN_TYPE_PROGRAM) {
            st = root.getSymbolTable();
            HashSet<String> set = new HashSet<>();

            for (Symbol s : st.symbols) {
                if (set.contains(s.GetName()))
                    System.out.println("Duplicated function found: " + s.GetType() + " " + s.GetName());
                else
                    set.add(s.GetName());
            }
        }

        if (root.getType() == TreeNodeType.TN_TYPE_ARGUMENTS) {
            st = root.getSymbolTable();
            HashSet<String> set = new HashSet<>();

            for (Symbol s : st.symbols) {
                if (set.contains(s.GetName()))
                    System.out.println("Duplicated arguments found: " + s.GetType() + " " + s.GetName());
                else
                    set.add(s.GetName());
            }
        }

        if (root.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK) {
            st = root.getSymbolTable();
            HashSet<String> set = new HashSet<>();

            for (Symbol s : st.symbols) {
                if (set.contains(s.GetName()))
                    System.out.println("Duplicated variables found: " + s.GetType() + " " + s.GetName());
                else
                    set.add(s.GetName());
            }
        }

        for (TreeNode node : root.getSubNodes())
            dupCheck(node, st);
    }

    private static void buildTable(TreeNode root, SymbolTable upperLevel) {
        if (root.getType() == TreeNodeType.TN_TYPE_PROGRAM) {
            SymbolTable table = new SymbolTable();
            for (TreeNode func : root.getSubNodes()) {
                String type = getLexeme(func.getTypeIndex());
                String name = getLexeme(func.getNameIndex());

                table.insert(new Symbol(name, type, 0, 0, 0));
                buildTable(func, table);
            }
            root.setSymbolTable(table);
        }

        if (root.getType() == TreeNodeType.TN_TYPE_FUNC) {
            SymbolTable table = new SymbolTable();
            table.setUpperTable(upperLevel);
            for (TreeNode node : root.getSubNodes()) {
                if (node.getType() == TreeNodeType.TN_TYPE_ARGUMENTS) {
                    for (TreeNode arg : node.getSubNodes()) {
                        String type = getLexeme(arg.getTypeIndex());
                        String name = getLexeme(arg.getNameIndex());

                        table.insert(new Symbol(name, type, 0, 0, 0));
                    }
                    node.setSymbolTable(table);
                }

                if (node.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK)
                    buildTable(node, table);
            }
            root.setSymbolTable(table);
        }

        if (root.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK) {
            SymbolTable table = new SymbolTable();
            table.setUpperTable(upperLevel);
            for (TreeNode stmt : root.getSubNodes()) {
                if (stmt.getType() == TreeNodeType.TN_TYPE_STMT_DECLARE) {
                    String type = getLexeme(stmt.getTypeIndex());
                    String name = getLexeme(stmt.getNameIndex());

                    table.insert(new Symbol(name, type, 0, 0, 0));
                }
            }
            root.setSymbolTable(table);
        }
    }

    private static void matchToken(Scanner.TKN_TYPE type) {
        if (tokenIndex < Scanner.getTokenType().size()) {
            Scanner.TKN_TYPE next = Scanner.getTokenType().get(tokenIndex);
            if (next != type)
                System.out.println("SOMETHING WRONG (matchToken)");
            else
                tokenIndex++;
        }
    }

    private static boolean isCnst() {
        nextToken = Scanner.getTokenType().get(tokenIndex);
        return nextToken == Scanner.TKN_TYPE.TKN_TYPE_CNST_INT || nextToken == Scanner.TKN_TYPE.TKN_TYPE_CNST_FLOAT ||
                nextToken == Scanner.TKN_TYPE.TKN_TYPE_CNST_STRING;
    }

    private static void matchCnst() {
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_CNST_STRING)
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_CNST_STRING);
        else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_CNST_INT)
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_CNST_INT);
        else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_CNST_FLOAT)
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_CNST_FLOAT);
        else {
            System.out.println("Something wrong! (cnst())");
        }
    }

    //Program     -> func_list
    private static TreeNode program() {
        TreeNode p = new TreeNode(TreeNodeType.TN_TYPE_PROGRAM);
        TreeNode fl = funcList();

        if(fl != null)
            p.getSubNodes().add(fl);

        return p;
    }

    //func_list   -> func func_list | e
    private static TreeNode funcList() {
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_ENDING)
            return null;
        else {
            TreeNode fl = new TreeNode(TreeNodeType.TN_TYPE_FUNC_LIST);
            TreeNode f = func();
            fl.getSubNodes().add(f);
            TreeNode fl2 = funcList();
            if(fl2 != null)
                fl.getSubNodes().add(fl2);
            return fl;
        }
    }

    //func -> type id '(' arguments ')' code_block
    private static TreeNode func() {
        int typeIndex, nameIndex;
        TreeNode f = new TreeNode(TreeNodeType.TN_TYPE_FUNC);
        TreeNode t = type();
        typeIndex = tokenIndex-1;

        TreeNode n = new TreeNode(TreeNodeType.TN_TYPE_ID);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_ID);
        nameIndex = tokenIndex - 1;

        f.setTknIndex(typeIndex, nameIndex);
        n.setTknIndex(typeIndex, nameIndex);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_OPEN_P);

        TreeNode a = arguments();
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P);
        TreeNode cb = codeBlock();

        f.getSubNodes().add(t);
        f.getSubNodes().add(n);
        if(a != null)
            f.getSubNodes().add(a);
        if(cb != null)
            f.getSubNodes().add(cb);
        return f;
    }

    // int | float | char
    private static TreeNode type() {
        String lexeme = Scanner.getTokenLexeme().get(tokenIndex);
        lexeme = lexeme.toLowerCase();
        if (lexeme.equals("int") || lexeme.equals("float") || lexeme.equals("char"))
            tokenIndex++;
        return new TreeNode(TreeNodeType.TN_TYPE_KEYWORD);
    }

    // e | arg_list
    private static TreeNode arguments() {
        TreeNode arg = new TreeNode(TreeNodeType.TN_TYPE_ARGUMENTS);
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken != Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P) {
            TreeNode args = argList();
            arg.getSubNodes().add(args);
            return arg;
        }
        return null;
    }

    // argument    -> type id
    private static TreeNode argument() {
        TreeNode a = new TreeNode(TreeNodeType.TN_TYPE_ARGUMENT);
        TreeNode t = type();
        int typeIndex = tokenIndex - 1;

        TreeNode n = new TreeNode(TreeNodeType.TN_TYPE_ID);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_ID);
        int nameIndex = tokenIndex - 1;

        a.setTknIndex(typeIndex, nameIndex);
        n.setTknIndex(typeIndex, nameIndex);
        n.setValUsed();

        a.getSubNodes().add(t);
        a.getSubNodes().add(n);

        return a;
    }

    // argument ',' arg_list | argument
    private static TreeNode argList() {
        TreeNode args = new TreeNode(TreeNodeType.TN_TYPE_ARGUMENTS);
        TreeNode arg = argument();
        args.getSubNodes().add(arg);
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_COMMA) {
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_COMMA);
            TreeNode al = argList();
            if (al != null)
                args.getSubNodes().add(al);
        }

        return args;
    }

    // '{' stmts '}'
    private static TreeNode codeBlock() {
        TreeNode code = new TreeNode(TreeNodeType.TN_TYPE_CODE_BLOCK);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_OPEN_CB);
        TreeNode sts = stmts();
        if (sts != null)
            code.getSubNodes().add(sts);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_CB);

        return code;
    }

    // stmt stmts | e
    private static TreeNode stmts() {
        TreeNode Stmts = new TreeNode(TreeNodeType.TN_TYPE_STMTS);
        nextToken = Scanner.getTokenType().get(tokenIndex);

        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_CB)
            return null;

        TreeNode stm = stmt();
        Stmts.getSubNodes().add(stm);
        TreeNode stms = stmts();
        if (stms != null)
            Stmts.getSubNodes().add(stms);
        return Stmts;
    }

    // assign_stmt | return_stmt | decl_stmt | func_call ';'
    private static TreeNode stmt() {
        nextToken = Scanner.getTokenType().get(tokenIndex);
        String lexeme = Scanner.getTokenLexeme().get(tokenIndex);
        lexeme = lexeme.toLowerCase();
        TreeNode tn = null;
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_ID) {
            if (Scanner.getTokenType().get(tokenIndex+1) == Scanner.TKN_TYPE.TKN_TYPE_PNCT_OPEN_P) {
                tn = func_call();
                matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_SEMICOLON);
            }

            else
                tn = assignStmt();
        }
        else if (lexeme.equals("int") || lexeme.equals("float") || lexeme.equals("char"))
            tn = declarStmt();
        else if (lexeme.equals("return"))
            tn = returnStmt();
        else
            System.out.println("SOMETHING WRONG! (stmt)");
        return tn;
    }

    // id '=' expr ';'
    private static TreeNode assignStmt() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_STMT_ASSIGN);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_ID);
        tn.setTknIndex(tokenIndex-1);

        TreeNode idt = new TreeNode(TreeNodeType.TN_TYPE_ID);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_OP_ASSIGN);
        TreeNode exp = expr1();
        tn.getSubNodes().add(idt);
        tn.getSubNodes().add(exp);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_SEMICOLON);

        return tn;
    }

    //type id ';'
    private static TreeNode declarStmt() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_STMT_DECLARE);
        TreeNode typ = type();
        int typeIndex = tokenIndex-1;

        TreeNode idt = new TreeNode(TreeNodeType.TN_TYPE_ID);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_ID);
        int nameIndex = tokenIndex-1;
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_SEMICOLON);

        tn.getSubNodes().add(typ);
        tn.getSubNodes().add(idt);

        tn.setTknIndex(typeIndex, nameIndex);
        idt.setTknIndex(typeIndex, nameIndex);
        return tn;
    }

    //return expr ';'
    private static TreeNode returnStmt() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_STMT_RETURN);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_KEYWORD);
        TreeNode exp = expr1();
        tn.getSubNodes().add(exp);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_SEMICOLON);

        return tn;
    }

    //    expr        -> term expr'
    private static TreeNode expr1() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_EXPR1);
        TreeNode tm1 = term1();
        tn.getSubNodes().add(tm1);

        TreeNode exp = expr2();
        if (exp != null)
            tn.getSubNodes().add(exp);

        return tn;
    }

    //    expr'       -> '+' term expr' | '-' term expr' | e
    private static TreeNode expr2() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_EXPR2);
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_OP_PLUS) {
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_OP_PLUS);
            TreeNode op = new TreeNode(TreeNodeType.TN_TYPE_OP_ADD);
            tn.getSubNodes().add(op);
        }
        else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_OP_MINUS) {
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_OP_MINUS);
            TreeNode op = new TreeNode(TreeNodeType.TN_TYPE_OP_SUB);
            tn.getSubNodes().add(op);
        }

        else {
            if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_SEMICOLON)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_COMMA)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_CB)
                return null;

            else System.out.println("ERROR: Something wrong (expr2)");
        }
        TreeNode tm1 = term1();
        tn.getSubNodes().add(tm1);
        TreeNode exp2 = expr2();
        if (exp2 != null)
            tn.getSubNodes().add(exp2);

        return tn;
    }

    //term        -> factor term'
    private static TreeNode term1() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_TERM1);
        TreeNode fc = factor();
        tn.getSubNodes().add(fc);

        TreeNode tm2 = term2();
        if (tm2 != null)
            tn.getSubNodes().add(tm2);

        return tn;
    }

    //term'       -> '*' factor term' | '/' factor term' | e
    private static TreeNode term2() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_TERM2);
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_OP_MULT) {
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_OP_MULT);
            TreeNode op = new TreeNode(TreeNodeType.TN_TYPE_OP_MUL);
            tn.getSubNodes().add(op);
        }

        else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_OP_DIV) {
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_OP_DIV);
            TreeNode op = new TreeNode(TreeNodeType.TN_TYPE_OP_DIV);
            tn.getSubNodes().add(op);
        }

        else {
            if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_OP_PLUS)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_OP_MINUS)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_SEMICOLON)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_COMMA)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P)
                return null;

            else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_CB)
                return null;

            else System.out.println("Something wrong (term2)");
        }
        TreeNode fc = factor();
        tn.getSubNodes().add(fc);

        TreeNode tm2 = term2();
        if (tm2 != null)
            tn.getSubNodes().add(tm2);

        return tn;
    }

    //'(' expr ')' | id | const | func_call
    private static TreeNode factor() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_FACTOR);
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_OPEN_P) {
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_OPEN_P);
            TreeNode exp1 = expr1();
            tn.getSubNodes().add(exp1);
            matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P);
        }
        else if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_ID) {
            if (Scanner.getTokenType().get(tokenIndex+1) == Scanner.TKN_TYPE.TKN_TYPE_PNCT_OPEN_P) {
                TreeNode fcall = func_call();
                tn.getSubNodes().add(fcall);
            }

            else {
                TreeNode id = new TreeNode(TreeNodeType.TN_TYPE_ID);
                matchToken(Scanner.TKN_TYPE.TKN_TYPE_ID);
                id.setTknIndex(tokenIndex-1);
                id.setTknIndex(-1, tokenIndex-1);
                id.setValUsed();
                tn.getSubNodes().add(id);
            }
        }

        else if (isCnst()) {
            matchCnst();
            TreeNode cn = new TreeNode(TreeNodeType.TN_TYPE_CONST);
            cn.setTknIndex(tokenIndex-1);
            tn.getSubNodes().add(cn);
        }

        return tn;

    }

    //id '(' expr_list ')'
    private static TreeNode func_call() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_FUNCCALL);
        TreeNode id = new TreeNode(TreeNodeType.TN_TYPE_ID);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_ID);
        id.setTknIndex(tokenIndex-1);
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_OPEN_P);
        TreeNode exp = expr_list();
        matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P);

        tn.getSubNodes().add(id);
        if (exp != null)
            tn.getSubNodes().add(exp);

        return tn;
    }

    //expr | expr ',' expr_list | e
    private static TreeNode expr_list() {
        TreeNode tn = new TreeNode(TreeNodeType.TN_TYPE_EXPRS);
        nextToken = Scanner.getTokenType().get(tokenIndex);
        if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P)
            return null;
        else {
            TreeNode exp = expr1();
            tn.getSubNodes().add(exp);
            if (nextToken == Scanner.TKN_TYPE.TKN_TYPE_PNCT_COMMA) {
                matchToken(Scanner.TKN_TYPE.TKN_TYPE_PNCT_COMMA);
                TreeNode exps = expr_list();
                if (exps != null)
                    tn.getSubNodes().add(exps);
            }
        }

        return tn;
    }
}
