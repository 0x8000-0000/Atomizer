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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class ClassRef
{
   private String className;

   private HashSet<String> dependsOn;

   private int color;

   public ClassRef(InputStream stream) throws IOException
   {
      dependsOn = new HashSet<String>();

      ClassReader reader = new ClassReader(stream);
      ClassNode classNode = new ClassNode();
      reader.accept(classNode, 0);

      className = classNode.name.replace('/', '.');

      dependsOn.add(classNode.superName.replace('/', '.'));

      for (Object itf : classNode.interfaces)
      {
         dependsOn.add(((String) itf).replace('/', '.'));
      }

      for (Object mn : classNode.methods)
      {
         MethodNode methodNode = (MethodNode) mn;

         Type type = Type.getType(methodNode.desc);
         dependsOn.add(type.getReturnType().getClassName());
         for (Type argType : type.getArgumentTypes())
         {
            dependsOn.add(argType.getClassName());
         }

         for (Object oo : methodNode.exceptions)
         {
            String excClass = (String) oo;
            dependsOn.add(excClass.replace('/', '.'));
         }

         if (null != methodNode.localVariables)
         {
            for (Object lon : methodNode.localVariables)
            {
               LocalVariableNode localVariableNode = (LocalVariableNode) lon;
               dependsOn.add(Type.getType(localVariableNode.desc).getClassName());
            }
         }
      }

      dependsOn.remove("void");
      dependsOn.remove("long");
      dependsOn.remove("int");
      dependsOn.remove("boolean");

      // the constructor adds it
      dependsOn.remove(className);
   }

   public String getClassName()
   {
      return className;
   }

   public Set<String> getDependencies()
   {
      return dependsOn;
   }

   /*
    * Used by the DFS algorithm for topological sort
    */
   public void markNew()
   {
      color = 0;
   }

   public boolean isNew()
   {
      return (0 == color);
   }

   public void markTemporary()
   {
      color = 1;
   }

   public boolean isTemporary()
   {
      return (1 == color);
   }

   public void markFinal()
   {
      color = 2;
   }

   public boolean isFinal()
   {
      return (2 == color);
   }

   @Override
   public String toString()
   {
      return className;
   }
}
