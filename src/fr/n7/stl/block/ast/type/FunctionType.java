/**
 * 
 */
package fr.n7.stl.block.ast.type;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.n7.stl.block.ast.SemanticsUndefinedException;
import fr.n7.stl.block.ast.scope.Declaration;
import fr.n7.stl.block.ast.scope.HierarchicalScope;
import fr.n7.stl.util.Logger;

/**
 * Implementation of the Abstract Syntax Tree node for a function type.
 * @author Marc Pantel
 *
 */
public class FunctionType implements Type {

	private Type result;
	private List<Type> parameters;

	public FunctionType(Type _result, Iterable<Type> _parameters) {
		this.result = _result;
		this.parameters = new LinkedList<Type>();
		for (Type _type : _parameters) {
			this.parameters.add(_type);
		}
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Type#equalsTo(fr.n7.stl.block.ast.Type)
	 */
	@Override
	public boolean equalsTo(Type _other) {
		boolean ok=true;
		if(_other instanceof FunctionType) {
			if(((FunctionType)_other).equalsTo(this.result)) {
				if(((FunctionType)_other).parameters.size()==this.parameters.size()) {
					for(int i=0;i<this.parameters.size();i++) {
						Type t1 = this.parameters.get(i);
						Type t2 = ((FunctionType) _other).parameters.get(i);
						ok = ok&& t1.equalsTo(t2);
					}
				}
				Logger.error(((FunctionType)_other).parameters+ "a un size different de celui des parametres");
				ok=false;
			}
			ok=false;
			Logger.error(((FunctionType)_other)+ " a un resultat different que FunctionType");
		}
		Logger.error(_other + "nest pas une instance de FunctionType");
		ok=false;
		return ok;
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Type#compatibleWith(fr.n7.stl.block.ast.Type)
	 */
	@Override
	public boolean compatibleWith(Type _other) {
		boolean ok=true;
		FunctionType fother = (FunctionType)_other;
		if (fother.result.compatibleWith(this.result)) {
			if(fother.parameters.size()==this.parameters.size()) {
				for(int i =0; i< this.parameters.size();i++) {
					ok = ok && this.parameters.get(i).compatibleWith(fother.parameters.get(i));
				}
			}else {
				Logger.error(fother.parameters+" a un size defferent des parametres");
				ok=false;
			}
		}else {
			Logger.error(fother.result+" n est pas compatible avec FunctionType");
			ok=false;
		}
		return ok;
		
		
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Type#merge(fr.n7.stl.block.ast.Type)
	 */
	@Override
	public Type merge(Type _other) {
		Type t;
		if(_other instanceof FunctionType) {
			t=this;
		}else {
			Logger.error(_other+ "a un type different que FunctionType");
			t=AtomicType.ErrorType;
		}
		return t;
		
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Type#length(int)
	 */
	@Override
	public int length() {
		return this.result.length();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String _result = "(";
		Iterator<Type> _iter = this.parameters.iterator();
		if (_iter.hasNext()) {
			_result += _iter.next();
			while (_iter.hasNext()) {
				_result += " ," + _iter.next();
			}
		}
		return _result + ") -> " + this.result;
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.type.Type#resolve(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean resolve(HierarchicalScope<Declaration> _scope) {
		boolean ok= this.result.resolve(_scope);
		for(Type t : this.parameters) {
			ok=ok&&t.resolve(_scope);
		}
		return ok;
	}
	

}
