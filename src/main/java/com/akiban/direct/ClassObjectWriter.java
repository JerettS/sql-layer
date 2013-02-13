/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */
package com.akiban.direct;

import java.util.Stack;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class ClassObjectWriter extends ClassBuilder {

    final ClassPool classPool;
    Stack<CtClass> ctClasses = new Stack<CtClass>();
    CtClass currentCtClass;

    public ClassObjectWriter(ClassPool classPool, String packageName, String schema) {
        super(packageName, schema);
        this.classPool = classPool;
    }

    @Override
    public void preamble(String[] imports) {
        // Nothing to do. All names are fully qualified.
    }

    @Override
    public void startClass(String name, boolean isInterface) {
        if (isInterface) {
            currentCtClass = classPool.makeInterface(name);
        } else {
            currentCtClass = classPool.makeClass(name);
        }
        ctClasses.push(currentCtClass);
    }

    @Override
    public void addMethod(String name, String returnType, String[] argumentTypes, String[] argumentNames, String[] body) {
        try {
            if (currentCtClass.isInterface()) {
                assert body == null;
            }
            CtClass returnClass = getCtClass(returnType);
            CtClass[] parameters = new CtClass[argumentTypes.length];
            for (int i = 0; i < argumentTypes.length; i++) {
                parameters[i] = getCtClass(argumentTypes[i]);
            }
            CtMethod method = new CtMethod(returnClass, name, parameters, currentCtClass);
            if (body != null) {
                StringBuilder sb = new StringBuilder("{");
                for (final String s : body) {
                    sb.append(s);
                }
                sb.append("}");
                method.insertAfter(sb.toString());
            }
            currentCtClass.addMethod(method);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void end() {
        currentCtClass = ctClasses.pop();
    }

    private CtClass getCtClass(final String name) {
        if ("void".equals(name)) {
            return CtClass.voidType;
        }
        if ("boolean".equals(name)) {
            return CtClass.booleanType;
        }
        if ("byte".equals(name)) {
            return CtClass.byteType;
        }
        if ("char".equals(name)) {
            return CtClass.charType;
        }
        if ("short".equals(name)) {
            return CtClass.shortType;
        }
        if ("int".equals(name)) {
            return CtClass.intType;
        }
        if ("float".equals(name)) {
            return CtClass.floatType;
        }
        if ("long".equals(name)) {
            return CtClass.longType;
        }
        if ("double".equals(name)) {
            return CtClass.doubleType;
        }
        CtClass c = classPool.getOrNull(name);
        if (c == null) {
            c = classPool.makeInterface(name);
        }
        return c;
    }

}
