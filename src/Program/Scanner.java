package Program;

import java.util.ArrayList;

class Scanner {
    private static char[] Delimiters = {'(', ')', '{', '}', '.', ',', ';', '[', ']'};
    private static char[] Operators = {'=', ':', '+', '-', '*', '/', '%', '<', '>'};
    private static String[] Keywords = {"auto", "break", "char", "const", "continue", "default",
            "do", "double", "else", "enum", "extern", "float", "for",
            "goto", "if", "inline", "int", "long", "register", "restrict",
            "return", "short", "signed", "sizeof", "static", "struct", "switch",
            "typedef", "union", "unsigned", "void", "volatile", "while"};

    enum States { q0, q1, q2, q3, q4 }

    enum TKN_TYPE {
        TKN_TYPE_KEYWORD,
        TKN_TYPE_ID,
        TKN_TYPE_CNST_INT,
        TKN_TYPE_CNST_FLOAT,
        TKN_TYPE_CNST_STRING,
        TKN_TYPE_OP_ASSIGN,
        TKN_TYPE_OP_PLUS,
        TKN_TYPE_OP_MINUS,
        TKN_TYPE_OP_MULT,
        TKN_TYPE_OP_DIV,
        TKN_TYPE_OP_MOD,
        TKN_TYPE_OP_LESS,
        TKN_TYPE_OP_MORE,
        TKN_TYPE_OP_COLON,
        TKN_TYPE_PNCT_SEMICOLON,
        TKN_TYPE_PNCT_OPEN_P,
        TKN_TYPE_PNCT_CLOSE_P,
        TKN_TYPE_PNCT_OPEN_B,
        TKN_TYPE_PNCT_CLOSE_B,
        TKN_TYPE_PNCT_OPEN_CB,
        TKN_TYPE_PNCT_CLOSE_CB,
        TKN_TYPE_PNCT_COMMA,
        TKN_TYPE_PNCT_DOT,
        TKN_TYPE_ENDING
    }
    private static int line = 0, col = 0;
    private static int currentLength = Integer.MAX_VALUE;

    private static ArrayList<TKN_TYPE> tokenType = new ArrayList<>();
    private static ArrayList<String> tokenLexeme = new ArrayList<>();

    static ArrayList<TKN_TYPE> getTokenType() {
        return tokenType;
    }

    static ArrayList<String> getTokenLexeme() {
        return tokenLexeme;
    }

    static void run() {
        System.out.println("Token list after scanning:\n");
        boolean stream = true;
        boolean floating = false;
        char currentCharacter = ' ';
        StringBuilder lexem = new StringBuilder();
        States state = States.q0;

        while (true) {
            if (stream)
                currentCharacter = getNextChar();

            if (currentCharacter == Character.MAX_VALUE)
                break;

            switch (state) {
                case q0:
                    if (isAlpha(currentCharacter)) {
                        state = States.q1;
                        lexem.append(currentCharacter);
                    }
                    else if (isDigit(currentCharacter)) {
                        state = States.q3;
                        lexem.append(currentCharacter);
                    }
                    else if (isOperator(currentCharacter)) {
                        state = States.q2;
                        lexem.append(currentCharacter);
                    }
                    else if (isDelimiter(currentCharacter)) {
                        ChooseDelimiter(currentCharacter);
                        state = States.q0;
                        lexem = new StringBuilder();
                    }
                    else if (currentCharacter == '"' || currentCharacter == '\'') {
                        state = States.q4;
                        lexem.append(currentCharacter);
                    }
                    else if (currentCharacter == ' ') {
                        stream = true;
                        break;
                    }
                    else {
                        System.out.println("ERROR in state 0");
                        System.out.println(currentCharacter);
                    }
                    stream = true;
                    break;
                case q1:
                    if (isAlphaOrDigit(currentCharacter))
                        lexem.append(currentCharacter);
                    else {
                        if (isKeyword(lexem.toString().toLowerCase())) {
                            //System.out.println("KEYWORD: " + lexem);
                            tokenType.add(TKN_TYPE.TKN_TYPE_KEYWORD);
                            tokenLexeme.add(lexem.toString());
                        }

                        else {
                            //System.out.println("ID: " + lexem);
                            tokenType.add(TKN_TYPE.TKN_TYPE_ID);
                            tokenLexeme.add(lexem.toString());
                        }

                        lexem = new StringBuilder();
                        state = States.q0;
                        stream = false;
                    }
                    break;

                case q2:
                    if (isOperator(currentCharacter))
                        lexem.append(currentCharacter);
                    else {
                        //System.out.println("OP: " + lexem);
                        ChooseOperator(lexem.toString());
                        state = States.q0;
                        lexem = new StringBuilder();
                        stream = false;
                    }
                    break;

                case q3:
                    if (isDigit(currentCharacter))
                        lexem.append(currentCharacter);
                    else if (currentCharacter == '.') {
                        floating = true;
                        lexem.append(currentCharacter);
                    }
                    else {
                        if (floating) {
                            //System.out.println("CONST: " + lexem);
                            tokenType.add(TKN_TYPE.TKN_TYPE_CNST_FLOAT);
                            tokenLexeme.add(lexem.toString());
                            floating = false;
                        }
                        else {
                            //System.out.println("CONST: " + lexem);
                            tokenType.add(TKN_TYPE.TKN_TYPE_CNST_INT);
                            tokenLexeme.add(lexem.toString());
                        }
                        state = States.q0;
                        lexem = new StringBuilder();
                        stream = false;
                    }
                    break;

                case q4:
                    if (currentCharacter == '"' || currentCharacter == '\'') {
                        lexem.append(currentCharacter);
                        tokenType.add(TKN_TYPE.TKN_TYPE_CNST_STRING);
                        tokenLexeme.add(lexem.toString());
                        //System.out.println("LEXEME: " + lexem);
                        state = States.q0;
                        lexem = new StringBuilder();
                    }
                    else
                        lexem.append(currentCharacter);
                    break;
                default:
                    System.out.println("ERROR!");
                    break;
            }
        }
        tokenType.add(TKN_TYPE.TKN_TYPE_ENDING);
        tokenLexeme.add("$");
        printTokens();
    }

    private static void printTokens() {
        int tokenIndex = 0;
        for (int i = 0; i < tokenType.size(); i++) {
            System.out.println(tokenIndex + ": " + tokenType.get(tokenIndex) + " " + tokenLexeme.get(tokenIndex++));
        }
    }

    private static void ChooseDelimiter(char currentCharacter) {
        switch (currentCharacter) {
            case '(':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_OPEN_P);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case ')':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_CLOSE_P);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case '{':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_OPEN_CB);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case '}':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_CLOSE_CB);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case '[':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_OPEN_B);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case ']':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_CLOSE_B);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case ';':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_SEMICOLON);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case '.':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_DOT);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            case ',':
                tokenType.add(TKN_TYPE.TKN_TYPE_PNCT_COMMA);
                tokenLexeme.add(String.valueOf(currentCharacter));
                break;
            default:
                System.out.println("ERROR IN DEL SCANNING");
                break;
        }
    }

    private static void ChooseOperator(String lexeme) {
        switch (lexeme) {
            case "=":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_ASSIGN);
                tokenLexeme.add(lexeme);
                break;
            case "+":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_PLUS);
                tokenLexeme.add(lexeme);
                break;
            case "-":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_MINUS);
                tokenLexeme.add(lexeme);
                break;
            case "*":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_MULT);
                tokenLexeme.add(lexeme);
                break;
            case "/":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_DIV);
                tokenLexeme.add(lexeme);
                break;
            case "%":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_MOD);
                tokenLexeme.add(lexeme);
                break;
            case "<":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_LESS);
                tokenLexeme.add(lexeme);
                break;
            case ">":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_MORE);
                tokenLexeme.add(lexeme);
                break;
            case ":":
                tokenType.add(TKN_TYPE.TKN_TYPE_OP_COLON);
                tokenLexeme.add(lexeme);
                break;
            default:
                System.out.println("ERROR IN OPERATOR");
                break;
        }
    }

    private static boolean isKeyword(String lexem) {
        for (String a : Keywords)
            if (a.equals(lexem))
                return true;
        return false;
    }

    private static boolean isOperator(char currentCharacter) {
        for (char a : Operators)
            if (currentCharacter == a)
                return true;
        return false;
    }

    private static boolean isDelimiter(char currentCharacter) {
        for (char a : Delimiters)
            if (currentCharacter == a)
                return true;
        return false;
    }

    private static boolean isDigit(char currentCharacter) {
        return Character.isDigit(currentCharacter);
    }

    private static boolean isAlpha(char currentCharacter) {
        return Character.isAlphabetic(currentCharacter);
    }

    private static boolean isAlphaOrDigit(char currentCharacter) {
        return Character.isAlphabetic(currentCharacter) || Character.isDigit(currentCharacter);
    }

    private static char getNextChar() {
        char c;
        if (col >= currentLength) {
            line++;
            col = 0;
        }

        if (line >= PreProcessor.getLines().size())
            return Character.MAX_VALUE;

        String tmp = PreProcessor.getLines().get(line);
        currentLength = tmp.length();

        while (tmp.length() == 0) {
            if (line + 1 == PreProcessor.getLines().size())
                return Character.MAX_VALUE;
            tmp = PreProcessor.getLines().get(++line);
            currentLength = tmp.length();
        }

        c = tmp.charAt(col++);
        return c;
    }
}
