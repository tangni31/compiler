package cop5556fa18;


/*
 * Ni Tang
 * Project #6
 * Date Assigned:November 6, 2018
 * Date Due:November 20, 2018
 * 
 */
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPTypes.Type;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	private int slot = 1;
	//HashMap<String, Integer>slotMap = new HashMap<String, Integer>();
	
	
	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		Label l0 = new Label();
		mv.visitLabel(l0);
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, arg);
		}
		Label l1 = new Label();
		mv.visitLabel(l1);

		for (PLPASTNode node : block.declarationsAndStatements) {
			if(node instanceof VariableDeclaration){
				VariableDeclaration declaration = (VariableDeclaration) node;
				String name = declaration.name;
				if(declaration.getType().equals(Type.BOOLEAN)){
					mv.visitLocalVariable(declaration.name, "Z", null, l0, 
							l1, declaration.getSlot(name));
				}
				else if(declaration.getType().equals(Type.FLOAT)){
					mv.visitLocalVariable(declaration.name, "F", null, l0, 
							l1, declaration.getSlot(name));
				}
				else if(declaration.getType().equals(Type.INTEGER)){
					mv.visitLocalVariable(declaration.name, "I", null, l0, 
							l1, declaration.getSlot(name));
				}
				else if(declaration.getType().equals(Type.CHAR)){
					mv.visitLocalVariable(declaration.name, "C", null, l0, 
							l1, declaration.getSlot(name));
				}
				else if(declaration.getType().equals(Type.STRING)){
					mv.visitLocalVariable(declaration.name, "Ljava/lang/String;", null, 
							l0, l1, declaration.getSlot(name));
				}
			}
			else if(node instanceof VariableListDeclaration){
				VariableListDeclaration declaration = (VariableListDeclaration) node;
				if(declaration.getType().equals(Type.BOOLEAN)){
					for(String name:declaration.names){
						mv.visitLocalVariable(name, "Z", null, l0, l1, 
								declaration.getSlot(name));
					}
				}
				else if(declaration.getType().equals(Type.FLOAT)){
					for(String name:declaration.names){
						mv.visitLocalVariable(name, "F", null, l0, l1, 
								declaration.getSlot(name));
						
					}
				}	
				else if(declaration.getType().equals(Type.INTEGER)){
					for(String name:declaration.names){
						mv.visitLocalVariable(name, "I", null, l0, l1, 
								declaration.getSlot(name));
					}
				}
				else if(declaration.getType().equals(Type.CHAR)){
					for(String name:declaration.names){
						mv.visitLocalVariable(name, "C", null, l0, l1, 
								declaration.getSlot(name));
					}
				}
				else if(declaration.getType().equals(Type.STRING)){
					for(String name:declaration.names){
						mv.visitLocalVariable(name, "Ljava/lang/String;", null, 
								l0, l1, declaration.getSlot(name));
					}
				}
			}
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();			
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		declaration.setSlot(declaration.name, slot);
		//slotMap.put(declaration.name, slot);
		slot ++;
		if(declaration.expression != null){
			Type t = declaration.getType();
			String name = declaration.name;
			declaration.expression.visit(this, arg);
			if(t.equals(Type.BOOLEAN) || t.equals(Type.INTEGER)){
				mv.visitVarInsn(Opcodes.ISTORE,declaration.getSlot(name));
			}
			else if(t.equals(Type.FLOAT)){
				mv.visitVarInsn(Opcodes.FSTORE, declaration.getSlot(name));
			}
			else if(t.equals(Type.STRING)){
				mv.visitVarInsn(Opcodes.ASTORE, declaration.getSlot(name));
			}
			else if(t.equals(Type.CHAR)){
				mv.visitIntInsn(Opcodes.ISTORE, declaration.getSlot(name));
			}
		}
		return null;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		for(String name: (declaration.names)){
			declaration.setSlot(name, slot);
			slot ++;
		}
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Label setTrue = new Label();
		Label l = new Label();
		
		Type t1 = expressionBinary.leftExpression.getType();
		Type t2 = expressionBinary.rightExpression.getType();
		Kind op = expressionBinary.op;
				
		if(op.equals(Kind.OP_PLUS)){
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(Opcodes.IADD);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(Opcodes.FADD);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.FADD);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER)){
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.FADD);
			}
			else if(t1.equals(Type.STRING) && t2.equals(Type.STRING)){
				mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
				mv.visitInsn(Opcodes.DUP);
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf",
						"(Ljava/lang/Object;)Ljava/lang/String;", false);
				mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", 
						"<init>", "(Ljava/lang/String;)V", false);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", 
						"append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", 
						"()Ljava/lang/String;", false);
			}
		}
		else if(op.equals(Kind.OP_MINUS)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.ISUB);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.FSUB);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.FSUB);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.FSUB);
			}
		}
		else if(op.equals(Kind.OP_TIMES)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.IMUL);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.FMUL);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.FMUL);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.FMUL);
			}
		}
		else if(op.equals(Kind.OP_DIV)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.IDIV);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.FDIV);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.SWAP);
				mv.visitInsn(Opcodes.FDIV);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.I2F);
				mv.visitInsn(Opcodes.FDIV);
			}
		}
		else if(op.equals(Kind.OP_POWER)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			if(t1.equals(Type.INTEGER) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.I2D);
				mv.visitVarInsn(DSTORE, 0);
				mv.visitInsn(Opcodes.I2D);
				mv.visitVarInsn(Opcodes.DLOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(Opcodes.D2I);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.F2D);
				mv.visitVarInsn(Opcodes.DSTORE, 0);
				mv.visitInsn(Opcodes.F2D);
				mv.visitVarInsn(Opcodes.DLOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(Opcodes.D2F);
			}
			else if(t1.equals(Type.INTEGER) && t2.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.F2D);
				mv.visitVarInsn(Opcodes.DSTORE, 0);
				mv.visitInsn(Opcodes.I2D);
				mv.visitVarInsn(Opcodes.DLOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(Opcodes.D2F);
			}
			else if(t1.equals(Type.FLOAT) && t2.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.I2D);
				mv.visitVarInsn(Opcodes.DSTORE, 0);
				mv.visitInsn(Opcodes.F2D);
				mv.visitVarInsn(Opcodes.DLOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(Opcodes.D2F);
			}
		}
		else if(op.equals(Kind.OP_MOD)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			mv.visitInsn(Opcodes.IREM);
		}
		else if(op.equals(Kind.OP_AND)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			mv.visitInsn(Opcodes.IAND);
		}
		else if(op.equals(Kind.OP_OR)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			mv.visitInsn(Opcodes.IOR);
		}
		else if(op.equals(Kind.OP_EQ)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(IF_ICMPEQ, setTrue);
			}
			else{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, setTrue);
			}
			mv.visitLdcInsn(false);
		}
		else if(op.equals(Kind.OP_NEQ)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(IF_ICMPNE, setTrue);
			}
			else{
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFNE, setTrue);
			}
			mv.visitLdcInsn(false);
		}
		else if(op.equals(Kind.OP_GT)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(Opcodes.IF_ICMPGT, setTrue);
			}
			else{
				mv.visitInsn(Opcodes.FCMPL);
				mv.visitJumpInsn(Opcodes.IFGT, setTrue);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_GE)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(Opcodes.IF_ICMPGE, setTrue);
			}
			else{
				mv.visitInsn(Opcodes.FCMPL);
				mv.visitJumpInsn(Opcodes.IFGE, setTrue);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_LT)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(Opcodes.IF_ICMPLT, setTrue);
			}
			else{
				mv.visitInsn(Opcodes.FCMPL);
				mv.visitJumpInsn(Opcodes.IFLT, setTrue);
			}
			mv.visitLdcInsn(false);
		}
		else if(expressionBinary.op.equals(Kind.OP_LE)){
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			if(t1.equals(Type.INTEGER) || t2.equals(Type.BOOLEAN)){
				mv.visitJumpInsn(Opcodes.IF_ICMPLE, setTrue);
			}
			else{
				mv.visitInsn(Opcodes.FCMPL);
				mv.visitJumpInsn(Opcodes.IFLE, setTrue);
			}
			mv.visitLdcInsn(false);
		}
		mv.visitJumpInsn(Opcodes.GOTO, l);
		mv.visitLabel(setTrue);
		mv.visitLdcInsn(true);
		mv.visitLabel(l);
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.condition.visit(this, arg);
		Label setTrue = new Label();
		Label lab = new Label();
		mv.visitJumpInsn(Opcodes.IFNE, setTrue);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(Opcodes.GOTO, lab);
		mv.visitLabel(setTrue);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitLabel(lab);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg functionWithArg, Object arg) throws Exception {
		functionWithArg.expression.visit(this, arg);
		Type t = functionWithArg.expression.getType();
		Kind funcName = functionWithArg.functionName;
		if(funcName.equals(Kind.KW_abs)){
			if(t.equals(Type.FLOAT)){
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
			}
			else if(t.equals(Type.INTEGER)){
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
			}
		}
		else if(funcName.equals(Kind.KW_atan)){
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		else if(funcName.equals(Kind.KW_sin)){
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		else if(funcName.equals(Kind.KW_cos)){
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		else if(funcName.equals(Kind.KW_log)){
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		else if(funcName.equals(Kind.KW_int)){
			if(t.equals(Type.FLOAT)){
				mv.visitInsn(Opcodes.F2I);
			}
		}
		else if(funcName.equals(Kind.KW_float)){
			if(t.equals(Type.INTEGER)){
				mv.visitInsn(Opcodes.I2F);
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		String name = expressionIdent.name;
		Type t = expressionIdent.getType();
		Declaration declaration = expressionIdent.getDec();
		if(t.equals(Type.BOOLEAN) || t.equals(Type.INTEGER)){
			mv.visitVarInsn(Opcodes.ILOAD, declaration.getSlot(name));
		}
		else if(t.equals(Type.FLOAT)){
			mv.visitVarInsn(Opcodes.FLOAD, declaration.getSlot(name));
		}
		else if(t.equals(Type.CHAR)){
			mv.visitVarInsn(Opcodes.ILOAD, declaration.getSlot(name));
		}
		else{
			mv.visitVarInsn(Opcodes.ALOAD, declaration.getSlot(name));
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		//mv.visitIntInsn(SIPUSH, expressionIntegerLiteral.value);
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		System.out.println(expressionStringLiteral.text.getClass());
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		//mv.visitIntInsn(BIPUSH, (int)expressionCharLiteral.text);
		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		statementAssign.expression.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		Type t = lhs.getType();
		Declaration d = lhs.getDec();
		if(t.equals(Type.BOOLEAN) || t.equals(Type.INTEGER)){
			mv.visitVarInsn(Opcodes.ISTORE, d.getSlot(lhs.identifier));
		}
		else if(t.equals(Type.FLOAT)){
			mv.visitVarInsn(Opcodes.FSTORE, d.getSlot(lhs.identifier));
		}
		else if(t.equals(Type.STRING)){
			mv.visitVarInsn(Opcodes.ASTORE, d.getSlot(lhs.identifier));
		}
		else if(t.equals(Type.CHAR)){
			mv.visitIntInsn(Opcodes.ISTORE, d.getSlot(lhs.identifier));
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.condition.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, l1);
		ifStatement.block.visit(this, arg);
		mv.visitLabel(l1);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label l1 = new Label();
		mv.visitJumpInsn(Opcodes.GOTO, l1);
		Label l2 = new Label();
		mv.visitLabel(l2);
		whileStatement.b.visit(this, arg);
		mv.visitLabel(l1);
		whileStatement.condition.visit(this, arg);
		mv.visitJumpInsn(Opcodes.IFNE, l2);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		/**
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch(type){
		case INTEGER : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
		}
		break;
		case BOOLEAN : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
		}
		break;
		case FLOAT : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
		}
		break; 
		case CHAR : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(C)V", false);
		}
		break;
		case STRING : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);
		}
		break;
		}
		return null;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, arg);
		mv.visitInsn(Opcodes.I2L);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Type t1 = expressionUnary.expression.getType();
		Kind op = expressionUnary.op;
		expressionUnary.expression.visit(this, arg);
		Label l0 = new Label();
		if(op.equals(Kind.OP_MINUS)){
			if(t1.equals(Type.INTEGER)){
				mv.visitInsn(INEG);
				mv.visitVarInsn(Opcodes.ISTORE, 0);
			}
			else if(t1.equals(Type.FLOAT)){
				mv.visitInsn(FNEG);
				mv.visitVarInsn(Opcodes.FSTORE, 0);
			}
		}
		else if(op.equals(Kind.OP_EXCLAMATION)){
			if(t1.equals(Type.INTEGER)){
				mv.visitLdcInsn(-1);
				mv.visitInsn(Opcodes.IXOR);
			}
			else if(t1.equals(Type.BOOLEAN)){
				Label l1 = new Label();
				mv.visitJumpInsn(IFEQ, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
		}
		if(t1.equals(Type.INTEGER) && op.equals(Kind.OP_MINUS)){
			mv.visitVarInsn(Opcodes.ILOAD, 0);
		}
		if(t1.equals(Type.FLOAT) && op.equals(Kind.OP_MINUS)){
			mv.visitVarInsn(Opcodes.FLOAD, 0);
		}
		mv.visitLabel(l0);
		return null;
	}

}
