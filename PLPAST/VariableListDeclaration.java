package cop5556fa18.PLPAST;

import java.util.List;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;

public class VariableListDeclaration extends Declaration {
	public final Kind type;
	public final List<String> names;
	
	public String names(int index) {
		return names.get(index);
	}
	
	public VariableListDeclaration(Token firstToken, Kind type, List<String> names) {
		super(firstToken);
		this.type = type;
		this.names = names;
	}

	@Override
	public Object visit(PLPASTVisitor v, Object arg) throws Exception {
		return v.visitVariableListDeclaration(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableListDeclaration other = (VariableListDeclaration) obj;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
