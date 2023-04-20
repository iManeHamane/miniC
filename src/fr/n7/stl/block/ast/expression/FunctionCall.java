/**
 * 
 */
package fr.n7.stl.block.ast.expression;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.block.ast.SemanticsUndefinedException;
import fr.n7.stl.block.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.block.ast.scope.Declaration;
import fr.n7.stl.block.ast.scope.HierarchicalScope;
import fr.n7.stl.block.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Abstract Syntax Tree node for a function call expression.
 * @author Marc Pantel
 *
 */
public class FunctionCall implements Expression {

	/**
	 * Name of the called function.
	 * TODO : Should be an expression.
	 */
	protected String name;
	
	/**
	 * Declaration of the called function after name resolution.
	 * TODO : Should rely on the VariableUse class.
	 */
	protected FunctionDeclaration function;
	
	/**
	 * List of AST nodes that computes the values of the parameters for the function call.
	 */
	protected List<Expression> arguments;
	
	/**
	 * @param _name : Name of the called function.
	 * @param _arguments : List of AST nodes that computes the values of the parameters for the function call.
	 */
	public FunctionCall(String _name, List<Expression> _arguments) {
		this.name = _name;
		this.function = null;
		this.arguments = _arguments;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String _result = ((this.function == null)?this.name:this.function) + "( ";
		Iterator<Expression> _iter = this.arguments.iterator();
		if (_iter.hasNext()) {
			_result += _iter.next();
		}
		while (_iter.hasNext()) {
			_result += " ," + _iter.next();
		}
		return  _result + ")";
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.expression.Expression#collect(fr.n7.stl.block.ast.scope.HierarchicalScope)
	 */
	@Override
	public boolean collectAndBackwardResolve(HierarchicalScope<Declaration> _scope) {
		//declaration takes the function code(miniC)
		Declaration decl = _scope.get(this.name);
		
		boolean ok = true; 
		this.function = (FunctionDeclaration)decl ;

			ok = (this.arguments.size() == function.getParameters().size());
			for (Expression expression : this.arguments) {
				ok = ok && expression.collectAndBackwardResolve(_scope);
			}
			return ok;
		 
			
			// si elle n'est pas définie il faut la déclarer.
			
		
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.expression.Expression#resolve(fr.n7.stl.block.ast.scope.HierarchicalScope)
	 */
	@Override
	public boolean fullResolve(HierarchicalScope<Declaration> _scope) {
		/* Declaration decl = _scope.get(this.name);
		boolean ok = true; 
		if(decl instanceof FunctionDeclaration) {
			System.out.println(" pas de problème en 1");
			this.function = (FunctionDeclaration)decl ;
			ok = (this.arguments.size() == function.getParameters().size());
			for (Expression expression : this.arguments) {
				ok = ok && expression.fullResolve(_scope);
				System.out.println(ok);
			}
			return ok;
		} else {
			Logger.error("fonction id :" + this.name + "n'est pas défini");
			return false;
			
		}*/
		boolean ok1 = _scope.contains(this.name);
		if (ok1) {
			this.function = (FunctionDeclaration) _scope.get(name);			
		} else {
			Logger.error("Error : FunctionCall");
			return false;
		}
		boolean ok2 = this.function.getParameters().size() != this.arguments.size();
		if (ok2) {
			Logger.error("Error : Number of arguments");
			return false;
		}
		
		return ok1;
		}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Expression#getType()
	 */
	@Override
	public Type getType() {
		return this.function.getType();
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Expression#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		
		Fragment code = _factory.createFragment();
		for (Expression arg : this.arguments) {
			code.append(arg.getCode(_factory));
		}
		code.add(_factory.createCall("function" + this.name, Register.SB));
		
		return code ; 
	}

}
