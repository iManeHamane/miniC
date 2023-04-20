/**
 * 
 */
package fr.n7.stl.block.ast.instruction;

import java.util.Optional;

import fr.n7.stl.block.ast.Block;
import fr.n7.stl.block.ast.SemanticsUndefinedException;
import fr.n7.stl.block.ast.expression.Expression;
import fr.n7.stl.block.ast.scope.Declaration;
import fr.n7.stl.block.ast.scope.HierarchicalScope;
import fr.n7.stl.block.ast.type.AtomicType;
import fr.n7.stl.block.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Implementation of the Abstract Syntax Tree node for a conditional instruction.
 * @author Marc Pantel
 *
 */
public class Conditional implements Instruction {

	protected Expression condition;
	protected Block thenBranch;
	protected Block elseBranch;

	public Conditional(Expression _condition, Block _then, Block _else) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = _else;
	}

	public Conditional(Expression _condition, Block _then) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "if (" + this.condition + " )" + this.thenBranch + ((this.elseBranch != null)?(" else " + this.elseBranch):"");
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndBackwardResolve(HierarchicalScope<Declaration> _scope) {
		
		boolean ok = this.condition.collectAndBackwardResolve(_scope) && this.thenBranch.collect(_scope);
		if(this.elseBranch!=null) {
			ok = ok && this.elseBranch.collect(_scope);
		}
			return ok;
		}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#resolve(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean fullResolve(HierarchicalScope<Declaration> _scope) {
		return true;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#checkType()
	 */
	@Override
	public boolean checkType() {
		
		// Check inside the () if it is a valid condition (ie a boolean) 
		Type t_cond = this.condition.getType();
		if(!t_cond.compatibleWith(AtomicType.BooleanType)) {
			Logger.error("check type condition");
			return false;
		}
		
		// Check type of then branch if the condition is valid 	
		boolean b = this.thenBranch.checkType();
		
		// Check if an else branch exists 
		if (this.elseBranch != null) {
			// Check its type
			b = b && this.elseBranch.checkType();
		}
		return b;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#allocateMemory(fr.n7.stl.tam.ast.Register, int)
	 */
	@Override
	public int allocateMemory(Register _register, int _offset) {
		this.thenBranch.allocateMemory(_register, _offset);
		if(this.elseBranch!=null) {
			this.elseBranch.allocateMemory(_register, _offset);
		}
		return _offset;
		
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment f = _factory.createFragment();
		
		// creating id of fragment 
		int id = _factory.createLabelNumber();
		
		f.append(this.condition.getCode(_factory));
		if (this.elseBranch != null) { //Else body exists 
			
			f.add(_factory.createJumpIf("Else Branch " + id, 0));
			
			f.append(this.thenBranch.getCode(_factory));
			f.add(_factory.createJump("End If " + id));
			f.addSuffix("Else Branch" + id );
			
			f.append(this.elseBranch.getCode(_factory));

		} else {
			f.add(_factory.createJump("End If " + id )); 
			f.append(this.thenBranch.getCode(_factory));			
		}
		
		f.addSuffix("End If " + id);		
		return f;
	}

}
