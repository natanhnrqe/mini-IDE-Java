package com.eyecode.lessons.practice;

import com.eyecode.editor.intelligence.document.DocumentSnapshot;
import com.eyecode.editor.intelligence.document.TextRange;
import com.eyecode.editor.v2.language.java.lexer.JavaTokenStream;
import com.eyecode.editor.v2.language.java.model.JavaClassModel;
import com.eyecode.editor.v2.language.java.model.JavaFileModel;
import com.eyecode.editor.v2.language.java.model.JavaMethodModel;
import com.eyecode.editor.v2.language.java.model.JavaVariableModel;
import com.eyecode.editor.v2.language.java.parser.JavaParser;
import com.eyecode.editor.v2.language.java.parser.ParserException;
import com.eyecode.language.ast.AstNode;
import com.eyecode.language.ast.AstNodeKind;
import com.eyecode.language.ast.AstNodes;
import com.eyecode.language.java.JavaLexerService;
import com.eyecode.language.java.JavaTokenType;
import com.eyecode.lessons.content.LessonPractice;

public final class PracticeValidator {
    private static final String INTEGER_SCORE = "integer-score";

    private final JavaLexerService lexerService;

    public PracticeValidator() {
        this(new JavaLexerService());
    }

    PracticeValidator(JavaLexerService lexerService) {
        this.lexerService = lexerService;
    }

    public PracticeVerificationResult verify(LessonPractice practice, String source) {
        if (practice == null || !INTEGER_SCORE.equals(practice.id())) {
            throw new IllegalArgumentException("Unsupported practice");
        }

        JavaFileModel model;
        try {
            model = parse(source);
        } catch (ParserException exception) {
            return result(PracticeVerificationStatus.SYNTAX_ERROR);
        }

        JavaClassModel mainClass = model.getTypes().stream()
                .filter(type -> "Main".equals(type.getName()))
                .findFirst().orElse(null);
        if (mainClass == null) return result(PracticeVerificationStatus.INVALID_CONTEXT);

        JavaMethodModel main = mainClass.getMethods().stream()
                .filter(method -> "main".equals(method.getName()))
                .findFirst().orElse(null);
        if (main == null) return result(PracticeVerificationStatus.INVALID_CONTEXT);

        JavaVariableModel score = main.getLocalVariables().stream()
                .filter(variable -> "score".equals(variable.getName()))
                .findFirst().orElse(null);
        if (score != null) {
            if (!"int".equals(score.getType())) return result(PracticeVerificationStatus.WRONG_TYPE);
            if (!hasIntegerLiteralInitializer(model.getAstRoot(), score.getRange(), "100")) {
                return result(PracticeVerificationStatus.WRONG_INITIALIZER);
            }
            return result(PracticeVerificationStatus.SUCCESS);
        }

        if (hasScoreOutsideMain(model, mainClass, main)) return result(PracticeVerificationStatus.INVALID_CONTEXT);
        if (main.getLocalVariables().stream().anyMatch(variable -> "int".equals(variable.getType()))) {
            return result(PracticeVerificationStatus.WRONG_NAME);
        }
        return result(PracticeVerificationStatus.MISSING_DECLARATION);
    }

    private JavaFileModel parse(String source) {
        String safeSource = source == null ? "" : source;
        return new JavaParser(new JavaTokenStream(
                lexerService.lex(DocumentSnapshot.oneShot(safeSource)).tokens(), safeSource)).parse();
    }

    private static boolean hasScoreOutsideMain(JavaFileModel model, JavaClassModel mainClass,
                                               JavaMethodModel main) {
        if (mainClass.getFields().stream().anyMatch(field -> "score".equals(field.getName()))) return true;
        return model.getTypes().stream()
                .flatMap(type -> type.getMethods().stream())
                .filter(method -> method != main)
                .flatMap(method -> method.getLocalVariables().stream())
                .anyMatch(variable -> "score".equals(variable.getName()));
    }

    private static boolean hasIntegerLiteralInitializer(AstNode root, TextRange variableRange, String expected) {
        if (root == null) return false;
        return AstNodes.descendants(root).stream()
                .filter(node -> node.kind() == AstNodeKind.LOCAL_VARIABLE_DECLARATION)
                .filter(node -> node.range().contains(variableRange))
                .flatMap(node -> node.children().stream())
                .filter(node -> node.kind() == AstNodeKind.DECLARATOR && node.range().contains(variableRange))
                .flatMap(node -> node.children().stream())
                .anyMatch(node -> node.kind() == AstNodeKind.LITERAL_EXPRESSION
                        && node.token() != null
                        && node.token().type() == JavaTokenType.NUMBER
                        && expected.equals(node.token().text()));
    }

    private static PracticeVerificationResult result(PracticeVerificationStatus status) {
        return new PracticeVerificationResult(status, switch (status) {
            case SUCCESS -> "Correto. Você declarou `score` como `int` e inicializou com `100`.";
            case SYNTAX_ERROR -> "O código Java ainda está incompleto ou possui um erro de sintaxe.";
            case INVALID_CONTEXT -> "Mantenha a declaração dentro do método `main`.";
            case MISSING_DECLARATION -> "Não encontrei a variável pedida dentro de `main`.";
            case WRONG_TYPE -> "Use o tipo `int` para esta variável.";
            case WRONG_NAME -> "A variável precisa se chamar `score`.";
            case WRONG_INITIALIZER -> "Inicialize `score` com o valor inteiro `100`.";
        });
    }
}
