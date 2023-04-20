/**
 * 
 */
package fr.n7.stl.block.ast.expression;

import fr.n7.stl.block.ast.SemanticsUndefinedException;
import fr.n7.stl.block.ast.scope.Declaration;
import fr.n7.stl.block.ast.scope.HierarchicalScope;
import fr.n7.stl.block.ast.type.AtomicType;
import fr.n7.stl.block.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Abstract Syntax Tree node for a conditional expression.
 * @author Marc Pantel
 *
 */
public class ConditionalExpression implements Expression {

	/**
	 * AST node for the expression whose value is the condition for the conditional expression.
	 */
	protected Expression condition;
	
	/**
	 * AST node for the expression whose value is the then parameter for the conditional expression.
	 */
	protected Expression thenExpression;
	
	/**
	 * AST node for the expression whose value is the else parameter for the conditional expression.
	 */
	protected Expression elseExpression;
	
	/**
	 * Builds a binary expression Abstract Syntax Tree node from the left and right sub-expressions
	 * and the binary operation.
	 * @param _left : Expression for the left parameter.
	 * @param _operator : Binary Operator.
	 * @param _right : Expression for the right parameter.
	 */
	public ConditionalExpression(Expression _condition, Expression _then, Expression _else) {
		this.condition = _condition;
		this.thenExpression = _then;
		this.elseExpression = _else;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.expression.Expression#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndBackwardResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok;
		ok = this.condition.collectAndBackwardResolve(_scope);
		if (this.elseExpression == null) {
			ok = ok && this.thenExpression.collectAndBackwardResolve(_scope);
		} else {
			ok = ok && this.thenExpression.collectAndBackwardResolve(_scope) 
			&& this.elseExpression.collectAndBackwardResolve(_scope);
		}
		return ok;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.expression.Expression#resolve(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean fullResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok; 
		ok = this.condition.fullResolve(_scope);
		if (this.elseExpression == null) {
			ok = ok && this.thenExpression.fullResolve(_scope);
		} else {
			ok = ok && this.thenExpression.fullResolve(_scope) 
			&& this.elseExpression.fullResolve(_scope);
		}
		return ok;
		
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + this.condition + " ? " + this.thenExpression + " : " + this.elseExpression + ")";
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Expression#getType()
	 */
	@Override
	public Type getType() {
		Type to;
		if(this.condition.getType().compatibleWith(AtomicType.BooleanType)) {
			if(this.thenExpression.getType().compatibleWith(this.elseExpression.getType())) {
				to= this.thenExpression.getType().merge(this.elseExpression.getType());
				
			}
			else {
				Logger.error("erreur dans le getType : then & else");
				to= AtomicType.ErrorType;
			}
		
		}
		else {
			Logger.error("erreur dans le getType : condition exp ");
			to= AtomicType.ErrorType;
		}
		return to;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Expression#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override 
	public Fragment getCode(TAMFactory _factory) {
		Fragment code = _factory.createFragment();
		int num=_factory.createLabelNumber();
		code.append(this.condition.getCode(_factory));
		//label
		code.add(_factory.createJumpIf("else "+num, 0));
		//if condition then
		code.append(this.thenExpression.getCode(_factory));
		//fin
		code.add(_factory.createJump("end "+num));
		code.addSuffix("else "+num);
		//else then
		code.append(this.elseExpression.getCode(_factory));
		code.addSuffix("end "+num);
		return code; 
	}

}
