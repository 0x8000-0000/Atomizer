/*
 * Copyright 2017 Florin Iucha <florin@signbit.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.signbit.tools.atomizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public class DumpClassReferences
{

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      long startTimeMillis = System.currentTimeMillis();
      Enumeration<? extends ZipEntry> en = zipFile.entries();
      while (en.hasMoreElements())
      {
         ZipEntry e = en.nextElement();
         String name = e.getName();
         if (name.endsWith(".class"))
         {
            InputStream stream = zipFile.getInputStream(e);
            processClass(stream);
         }
      }
      long endTimeMillis = System.currentTimeMillis();
   }

   private static void processClass(InputStream stream) throws IOException
   {
      ClassReader reader = new ClassReader(stream);
      ClassNode classNode = new ClassNode();
      reader.accept(classNode, 0);

      System.out.println("Class: " + classNode.name);
      System.out.println("   Extends: " + classNode.superName);

      for (Object itf : classNode.interfaces)
      {
         System.out.println("   Implements: " + itf);
      }

      for (Object mn : classNode.methods)
      {
         MethodNode methodNode = (MethodNode) mn;
         System.out.println("   Method: " + methodNode.name);

         Type type = Type.getType(methodNode.desc);
         System.out.println("      Return: " + type.getReturnType().getClassName());
         for (Type argType : type.getArgumentTypes())
         {
            System.out.println("      Param: " + argType.getClassName());
         }

         for (Object oo : methodNode.exceptions)
         {
            System.out.println("      Throws: " + oo);
         }

         for (Object lon : methodNode.localVariables)
         {
            LocalVariableNode localVariableNode = (LocalVariableNode) lon;
            System.out.println("      Var: " + Type.getType(localVariableNode.desc).getClassName());
         }
      }

   }
}
