/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package c10n.tools.codegen;

import c10n.C10NDef;
import c10n.C10NMessages;
import c10n.gen.C10NGenMessages;
import c10n.gen.C10NGenValue;
import c10n.share.utils.ReflectionUtils;
import c10n.tools.search.C10NInterfaceSearch;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import static japa.parser.ASTHelper.addMember;
import static japa.parser.ASTHelper.addParameter;
import static japa.parser.ASTHelper.addStmt;
import static japa.parser.ASTHelper.createParameter;

class DefaultCodeGenerator implements CodeGenerator {
  private final C10NInterfaceSearch search;
  private static final ClassOrInterfaceType stringType = new ClassOrInterfaceType(
          String.class.getName());
  private static final StringLiteralExpr emptyString = new StringLiteralExpr(
          "");
  private static final BooleanLiteralExpr falseExpr = new BooleanLiteralExpr(
          false);

  DefaultCodeGenerator(C10NInterfaceSearch search) {
    this.search = search;
  }

  public void convert(StringBuilder sb, Class<?> c10nInterface,
                      String typeName, Map<String, String> translations,
                      String localeSuffix) {
    sb.append(convert(c10nInterface, typeName, translations, localeSuffix)
            .toString());
  }

  public TypeDeclaration convert(Class<?> c10nInterface, String typeName,
                                 Map<String, String> translations, String localeSuffix) {
    ClassOrInterfaceDeclaration intface = new ClassOrInterfaceDeclaration(
            ModifierSet.PUBLIC, false,//
            typeName);
    intface.setImplements(list(new ClassOrInterfaceType(ReflectionUtils
            .getFQNString(c10nInterface))));

    List<AnnotationExpr> typeAnnotations = new ArrayList<AnnotationExpr>(1);
    typeAnnotations.add(new SingleMemberAnnotationExpr(ASTHelper
            .createNameExpr(C10NGenMessages.class.getName()),
            localeSuffix == null//
                    ? emptyString//
                    : new StringLiteralExpr(localeSuffix)));
    intface.setAnnotations(typeAnnotations);

    for (Method method : c10nInterface.getMethods()) {
      String key = ReflectionUtils.getDefaultKey(c10nInterface, method);
      String localisedValue = translations.get(key);

      MethodDeclaration m = new MethodDeclaration(ModifierSet.PUBLIC,
              stringType, method.getName());

      if (method.getDeclaringClass().equals(c10nInterface)) {
        JavadocComment jdc = new JavadocComment(" " + key + " ");
        m.setJavaDoc(jdc);

        List<AnnotationExpr> annotations = new ArrayList<AnnotationExpr>(
                1);
        List<MemberValuePair> pairs = new ArrayList<MemberValuePair>(2);
        pairs.add(new MemberValuePair("value",
                localisedValue != null ? new StringLiteralExpr(
                        localisedValue) : emptyString));
        if (localisedValue == null) {
          pairs.add(new MemberValuePair("defined", falseExpr));
        }

        annotations.add(new NormalAnnotationExpr(ASTHelper
                .createNameExpr(C10NGenValue.class.getName()), pairs));
        m.setAnnotations(annotations);
      }

      int paramId = 0;
      for (Class<?> paramType : method.getParameterTypes()) {
        addParameter(
                m,
                createParameter(convert(paramType), "arg" + (paramId++)));
      }
      BlockStmt block = new BlockStmt();

      m.setBody(block);
      addStmt(block, new ReturnStmt(new NullLiteralExpr()));
      addMember(intface, m);
    }
    return intface;
  }

  private static <E> List<E> list(E... args) {
    return Arrays.asList(args);
  }

  private static Type convert(Class<?> type) {
    if (type.isPrimitive()) {
      if (type.equals(Integer.TYPE)) {
        return ASTHelper.INT_TYPE;
      } else if (type.equals(Long.TYPE)) {
        return ASTHelper.LONG_TYPE;
      } else if (type.equals(Character.TYPE)) {
        return ASTHelper.CHAR_TYPE;
      } else if (type.equals(Boolean.TYPE)) {
        return ASTHelper.BOOLEAN_TYPE;
      }
    }
    return new ClassOrInterfaceType(type.getName());
  }

  @Override
  public void convert(StringBuilder sb, Class<?> c10nInterface) {
    sb.append("public class ");
    sb.append(c10nInterface.getSimpleName());
    sb.append("_def implements ");
    ReflectionUtils.getFQNString(c10nInterface, sb);
    sb.append('{');
    for (Method method : c10nInterface.getMethods()) {
      String defaultValue = "";
      C10NDef def = method.getAnnotation(C10NDef.class);
      if (null != def) {
        defaultValue = def.value();
      }
      sb.append("public String ");
      sb.append(method.getName());
      sb.append("(){ return \"");

      sb.append(defaultValue);
      sb.append("\"; }");
    }
    sb.append('}');
  }

  @Override
  public void convertAll(String packagePrefix, File outputSrcFolder,
                         File baseFile) throws IOException {
    Set<Class<?>> c10nIfs = search.find(packagePrefix, C10NMessages.class);
    String targetPackage = "c10n.gen";
    File targetFolder = new File(outputSrcFolder, "c10n/gen");
    List<ResourceFile> allFiles = findResourceFiles(baseFile);
    for (ResourceFile rf : allFiles) {
      generateSource(c10nIfs, targetPackage, targetFolder, rf);
    }
  }

  private void generateSource(Set<Class<?>> c10nIfs, String targetPackage,
                              File targetFolder, ResourceFile rf) throws IOException {
    if (!targetFolder.exists()) {
      if (!targetFolder.mkdirs()) {
        throw new IOException("Failed to create folder: "
                + targetFolder);
      }
    }

    Map<String, String> translations = loadTranslations(rf);

    for (Class<?> c10nInterface : c10nIfs) {
      generateSourceFile(c10nInterface, translations, targetPackage,
              targetFolder, rf.localeSuffix);
    }
  }

  private void generateSourceFile(Class<?> c10nInterface,
                                  Map<String, String> translations, String targetPackage,
                                  File targetFolder, String localeSuffix) throws IOException {
    String typeName;
    if (null != localeSuffix) {
      typeName = c10nInterface.getSimpleName() + "_" + localeSuffix
              + "_impl";
    } else {
      typeName = c10nInterface.getSimpleName() + "_impl";
    }
    CompilationUnit cu = new CompilationUnit();
    cu.setPackage(new PackageDeclaration(ASTHelper
            .createNameExpr(targetPackage)));
    TypeDeclaration typeDec = convert(c10nInterface, typeName,
            translations, localeSuffix);
    ASTHelper.addTypeDeclaration(cu, typeDec);

    File outputJavaFile = new File(targetFolder, typeName + ".java");
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(outputJavaFile);
      BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fos));
      w.append(cu.toString());
      w.flush();
    } finally {
      if (null != fos) {
        fos.close();
      }
    }
  }

  private Map<String, String> loadTranslations(ResourceFile rf)
          throws IOException {
    Properties prop = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(rf.file);
      prop.load(new InputStreamReader(in, Charset.forName("UTF-8")));
      Map<String, String> res = new HashMap<String, String>();
      for (Entry<Object, Object> entry : prop.entrySet()) {
        res.put(String.valueOf(entry.getKey()),
                String.valueOf(entry.getValue()));
      }
      return res;
    } finally {
      if (null != in) {
        in.close();
      }
    }
  }

  private List<ResourceFile> findResourceFiles(File baseFile) {
    File parentDir = baseFile.getParentFile();
    String baseName = baseFile.getName();
    List<ResourceFile> res = new ArrayList<ResourceFile>();
    for (File f : parentDir.listFiles()) {
      int p = f.getName().lastIndexOf(".properties");
      if (f.getName().startsWith(baseName) && p > 0) {
        String localeSuffix = null;
        if (p > baseName.length() + 1) {
          localeSuffix = f.getName().substring(baseName.length() + 1,
                  p);
        }
        res.add(new ResourceFile(f, localeSuffix));
      }
    }
    return res;
  }

  private static final class ResourceFile {
    private final File file;
    private final String localeSuffix;

    ResourceFile(File file, String localeSuffix) {
      this.file = file;
      this.localeSuffix = localeSuffix;
    }
  }
}
