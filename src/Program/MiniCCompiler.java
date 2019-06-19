package Program;

public class MiniCCompiler {
    public static void main(String [ ] args)
    {
        if (args.length < 1)
            System.out.println("USAGE: MiniCCompiler xxx.c");
        else
            System.out.println("Starting to compile file " + args[0]);

        run(args[0]);
    }

    private static void run (String file) {
        PreProcessor.run(file);
        Scanner.run();
        Parser.run();
        CodeGenerator.run();
    }
}
