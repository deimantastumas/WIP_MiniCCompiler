package Program;

import java.util.Stack;

public class CodeGenerator {
    public static void run() {
        System.out.println("Code generator...");
        TreeNode root = Parser.getRoot();

        String code = genHeader();
        code += genData();
        code += genCode(root);

        System.out.println(code);
    }

    private static String genHeader() {
        String code = "#\tThis is auto generated from MiniCCompiler\n";
        return code;
    }

    private static String genData() {
        String code = "\t.data\n";
        return code;
    }

    private static String genCode(TreeNode root) {
        allocateReg(root, null);
        root.PrintTree(root);

        String code = "\t.text\n";
        code += "__init:\n";
        code += "\t# setup the stack\n";
        code += "\tlui $sp, 0x8000\n";
        code += "\taddi $sp, $sp, 0x0000\n";

        code += "\t# redirect to main function\n";
        code += "\tjal __main\n";

        code += "\t# make system call to terminate the program\n";
        code += "\tli $v0, 10\n";
        code += "\tsyscall\n\n";

        code += genCode2(root, null);
        return code;
    }

    static int argCnt;
    private static void allocateReg(TreeNode root, SymbolTable st) {
        TreeNodeType type = root.getType();
        if (type == TreeNodeType.TN_TYPE_PROGRAM || type == TreeNodeType.TN_TYPE_FUNC){
            st = root.getSymbolTable();
            if (type == TreeNodeType.TN_TYPE_FUNC) {
                initRegs();
                argCnt = 0;
            }
        }

        if (type == TreeNodeType.TN_TYPE_FUNC) {
            if (root.getSubByIndex(2).getType() == TreeNodeType.TN_TYPE_ARGUMENTS) {
                TreeNode args = root.getSubByIndex(2);
                int i = 4;
                for(TreeNode arg : args.getSubNodes()) {
                    arg.setReg(i);
                    String name = Scanner.getTokenLexeme().get(arg.getSubByIndex(1).getNameIndex());
                    Symbol s = st.findByName(name);
                    if(s != null)
                        s.setReg(i++);
                }
                allocateReg(root.getSubByIndex(3), st);
            }
            else {
                allocateReg(root.getSubByIndex(2), st);
            }
        }
        else {
            for(TreeNode c : root.getSubNodes()){
                allocateReg(c, st);
            }
        }

        if(type == TreeNodeType.TN_TYPE_ID) {
            if (root.getValUsed()) {
                Symbol s = st.findByName(Scanner.getTokenLexeme().get(root.getNameIndex()));
                if (s != null)
                    root.setReg(s.getReg());
            }
        }
        else if(type == TreeNodeType.TN_TYPE_CONST) {
            int reg = getFreeReg();
            root.setReg(reg);
        }
        else if(type == TreeNodeType.TN_TYPE_EXPR1) {
            if(root.getSubNodes().size() == 3) {
                int reg = getFreeReg();
                root.setReg(reg);
            }
            else if(root.getSubNodes().size() == 1){
                root.setReg(root.getSubByIndex(0).getReg());
            }
        }
        else {
            if(root.getSubNodes().size() == 1){
                root.setReg(root.getSubByIndex(0).getReg());
            }
        }
    }

    private static int getFreeReg() {
        if(regStack.empty()){
            System.out.println("No registers available. Implement better algorithm!");
            System.exit(1);
        }

        return regStack.pop();
    }

    static Stack<Integer> regStack;
    private static void initRegs() {
        regStack = new Stack<>();
        regStack.push(8);
        regStack.push(9);
        regStack.push(10);
        regStack.push(11);
        regStack.push(12);
        regStack.push(13);
        regStack.push(14);
        regStack.push(15);
        regStack.push(16);
        regStack.push(17);
        regStack.push(18);
        regStack.push(19);
        regStack.push(20);
        regStack.push(21);
        regStack.push(22);
        regStack.push(23);
        regStack.push(24);
        regStack.push(25);
    }

    static SymbolTable currTable = null;
    private static String genCode2(TreeNode root, SymbolTable st) {
        String code = "";
        if (root == null) return code;

        if (root.getType() == TreeNodeType.TN_TYPE_FUNC) {
            st = root.getSymbolTable();
            currTable = root.getSymbolTable();
            int index = root.getSubByIndex(1).getNameIndex();
            String name = Scanner.getTokenLexeme().get(index);
            code += "__" + name + ":\n";

            code += "\taddiu $sp, $sp, -64\n";
            code += "\tsw $s0, 8($sp)\n";
            code += "\tsw $s1, 12($sp)\n";
            code += "\tsw $s2, 16($sp)\n";
            code += "\tsw $s3, 20($sp)\n";
            code += "\tsw $fp, 40($sp)\n";
            code += "\tsw $gp, 44($sp)\n";
            code += "\tsw $ra, 48($sp)\n";

            if (root.getSubNodes().size() == 4)
                code += genCode2(root.getSubByIndex(3), st);
            else
                code += genCode2(root.getSubByIndex(2), st);

            code += "\tlw $s0, 8($sp)\n";
            code += "\tlw $s1, 12($sp)\n";
            code += "\tlw $s2, 16($sp)\n";
            code += "\tlw $s3, 20($sp)\n";
            code += "\tlw $fp, 40($sp)\n";
            code += "\tlw $gp, 44($sp)\n";
            code += "\tlw $ra, 48($sp)\n";
            code += "\taddiu $sp, $sp, 64\n";

            code += "\tjr $ra\n";

        }
        else if (root.getType() == TreeNodeType.TN_TYPE_FUNCCALL) {
            TreeNode args = root.getSubByIndex(1);
            for (TreeNode arg: args.getSubNodes()) {
                code += genCode2(arg, st);
            }
            int i = 0;
            for (TreeNode arg: args.getSubNodes()) {
                code += "\taddi $a" + i + ", " + getRegName(arg.getReg()) + ", 0\n";
                i++;
            }

            String name = Scanner.getTokenLexeme().get(root.getSubByIndex(0).getTokenIndex());
            code += "\tjal __" + name + "\n";
        }
        else if (root.getType() == TreeNodeType.TN_TYPE_STMT_RETURN) {
            code += genCode2(root.getSubByIndex(0), st);
            code += "\taddi $v0, " + getRegName(root.getSubByIndex(0).getReg()) + ", 0\n"; // Necessary?
        }
        else if (root.getType() == TreeNodeType.TN_TYPE_EXPR1) {
            if (root.getSubNodes().size() == 3) {
                TreeNode op1 = root.getSubByIndex(0);
                TreeNode op2 = root.getSubByIndex(2);
                TreeNode op = root.getSubByIndex(1);

                code += genCode2(op1, st);
                code += genCode2(op2, st);

                if (op.getType() == TreeNodeType.TN_TYPE_OP_SUB) {
                    code += "\tsub " + getRegName(root.getReg());
                    code += ", " + getRegName(op1.getReg());
                    code += ", " + getRegName(op2.getReg()) + "\n";
                }
                else if (op.getType() == TreeNodeType.TN_TYPE_OP_ADD) {
                    code += "\taddu " + getRegName(root.getReg());
                    code += ", " + getRegName(op1.getReg());
                    code += ", " + getRegName(op2.getReg()) + "\n";
                }
                else if (op.getType() == TreeNodeType.TN_TYPE_OP_MUL) {
                    code += "\tmul " + getRegName(root.getReg());
                    code += ", " + getRegName(op1.getReg());
                    code += ", " + getRegName(op2.getReg()) + "\n";
                }
                else if (op.getType() == TreeNodeType.TN_TYPE_OP_DIV) {
                    code += "\tdiv " + getRegName(root.getReg());
                    code += ", " + getRegName(op1.getReg());
                    code += ", " + getRegName(op2.getReg()) + "\n";
                }

            }else {
                code += genCode2(root.getSubByIndex(0), st);
            }
        }else if(root.getType() == TreeNodeType.TN_TYPE_CONST) {
            String num = Scanner.getTokenLexeme().get(root.getTokenIndex());
            code += "\tli " + getRegName(root.getReg()) + ", " + num + "\n";
        }else {
            for(TreeNode n: root.getSubNodes()) {
                code += genCode2(n, st);
            }
        }

        return code;
    }

    private static String getRegName(int reg) {
        switch (reg) {
            case 2:
                return "$v0";
            case 4:
                return "$a0";
            case 5:
                return "$a1";
            case 6:
                return "$a2";
            case 7:
                return "$a3";
            case 8:
                return "$t0";
            case 9:
                return "$t1";
            case 10:
                return "$t2";
            case 11:
                return "$t3";
            case 12:
                return "$t4";
            case 13:
                return "$t5";
            case 14:
                return "$t6";
            case 15:
                return "$t7";
            case 16:
                return "$s0";
            case 17:
                return "$s1";
            case 18:
                return "$s2";
            case 19:
                return "$s3";
            case 20:
                return "$s4";
            case 21:
                return "$s5";
            case 22:
                return "$s6";
            case 23:
                return "$s7";
            case 24:
                return "$t8";
            case 25:
                return "$t9";
            default:
                return "UNKNOWN";
        }
    }
}
