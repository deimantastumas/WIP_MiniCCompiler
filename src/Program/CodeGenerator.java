package Program;

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
        String code = "\t.text\n";
        code += genCode2(root);
        return code;
    }

    private static String genCode2(TreeNode root) {
        String code = " ";
        if (root == null) return code;

        if (root.getType() == TreeNodeType.TN_TYPE_PROGRAM) {
            for (TreeNode funcs : root.getSubNodes()) {
                code += genCode2(funcs);
            }
        }

        if (root.getType() == TreeNodeType.TN_TYPE_FUNC) {
            String name = Parser.getLexeme(root.getNameIndex());
            code += name + ":\n";
            code += "\taddiu $sp, $sp, -64\n";

            code += "\tsw $s0, 8($sp)\n";
            code += "\tsw $s1, 12($sp)\n";
            code += "\tsw $s2, 16($sp)\n";
            code += "\tsw $s3, 20($sp)\n";
            code += "\tsw $fp, 40($sp)\n";
            code += "\tsw $gp, 44($sp)\n";
            code += "\tsw $ra, 48($sp)\n";

            if (root.getSubNodes().size() == 4)
                code += genCode2(root.getSubByIndex(3));
            else
                code += genCode2(root.getSubByIndex(2));

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
        else if (root.getType() == TreeNodeType.TN_TYPE_CODE_BLOCK) {
            for (TreeNode stmt : root.getSubNodes()) {

            }
        }
        else if (root.getType() == TreeNodeType.TN_TYPE_FUNCCALL) {

        }
        else if (root.getType() == TreeNodeType.TN_TYPE_EXPR1) {

        }

        return code;
    }
}
