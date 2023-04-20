/**
 * 
 */
package fr.n7.stl.block.ast.instruction;

import java.util.ArrayList;
import java.util.HashMap;

import fr.n7.stl.block.ast.SemanticsUndefinedException;
import fr.n7.stl.block.ast.expression.Expression;
import fr.n7.stl.block.ast.scope.Declaration;
import fr.n7.stl.block.ast.scope.HierarchicalScope;
import fr.n7.stl.block.ast.type.AtomicType;
import fr.n7.stl.block.ast.type.CoupleType;
import fr.n7.stl.block.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.tam.ast.TAMInstruction;
import fr.n7.stl.util.Logger;

/**
 * Implementation of the Abstract Syntax Tree node for a printer instruction.
 * @author Marc Pantel
 *
 */
public class Printer implements Instruction {

	protected Expression parameter;

	public Printer(Expression _value) {
		this.parameter = _value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "print " + this.parameter + ";\n";
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndBackwardResolve(HierarchicalScope<Declaration> _scope) {
		return this.parameter.collectAndBackwardResolve(_scope);
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#resolve(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean fullResolve(HierarchicalScope<Declaration> _scope) {
		return this.parameter.fullResolve(_scope);
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#checkType()
	 */
	@Override
	public boolean checkType() {
		Type[] types = {AtomicType.BooleanType, AtomicType.IntegerType, AtomicType.StringType, AtomicType.CharacterType, AtomicType.FloatingType };
		Type type = this.parameter.getType();
		
		for (Type t : types) {
			if (type.equalsTo(t)) {
				return true;
			}
		}
		if(type instanceof CoupleType) {
			return true;
			
		}
		Logger.error("Incompatible Printer type");
		return false ;
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#allocateMemory(fr.n7.stl.tam.ast.Register, int)
	 */
	@Override
	public int allocateMemory(Register _register, int _offset) {
		return _offset + this.parameter.getType().length();
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Instruction#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		
		HashMap<AtomicType, TAMInstruction> types = new HashMap<>();
		types.put(AtomicType.BooleanType, Library.BOut);
		types.put(AtomicType.IntegerType, Library.IOut);
		types.put(AtomicType.StringType, Library.SOut);
		types.put(AtomicType.CharacterType, Library.COut);
		
		Fragment f = _factory.createFragment();
		f.append(this.parameter.getCode(_factory));

		for ( AtomicType type : types.keySet()) {
			f.add(types.get(type));
			break;
		}
		
		f.add(_factory.createLoadL('\n'));
		f.add(Library.COut);
		
		return f;
	}

}
