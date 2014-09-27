package patchengine.util;

import org.mozilla.javascript.Decompiler;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.UintMap;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ParenthesizedExpression;


public class RhinoUtil {

    private RhinoUtil() {}

    public static int getArgumentCount(NativeFunction function) {
        Parser parser = new Parser();

        String output = Decompiler.decompile(function.getEncodedSource(), Decompiler.TO_SOURCE_FLAG, new UintMap(0));
        AstRoot root = parser.parse(output, "", 1);

        ExpressionStatement expression = (ExpressionStatement) root.getFirstChild();
        ParenthesizedExpression parenthesized = (ParenthesizedExpression) expression.getExpression();
        FunctionNode node = (FunctionNode) parenthesized.getExpression();

        return node.getParamCount();
    }
}
