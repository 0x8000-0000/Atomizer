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
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassRef
{
   private String jarName;

   private String packageName;

   private String className;

   private HashSet<String> fullDependencySet;

   private HashSet<ClassRef> resolvableDependencies;

   private int dependedOnByCount = 0;

   private int color;

   private ClassRef(InputStream stream, String jarName) throws IOException
   {
      this.jarName = jarName;
      fullDependencySet = new HashSet<>();

      ClassReader reader = new ClassReader(stream);
      ClassNode classNode = new ClassNode();
      reader.accept(classNode, 0);

      String[] packageElements = classNode.name.split("/");
      if (packageElements.length > 1)
      {
         String[] packageOnly = Arrays.copyOfRange(packageElements, 0, packageElements.length - 1);
         packageName = String.join(".", packageOnly);
      }
      else
      {
         packageName = "";
      }
      className = packageElements[packageElements.length - 1];

      fullDependencySet.add(classNode.superName.replace('/', '.'));

      for (Object itf: classNode.interfaces)
      {
         fullDependencySet.add(((String) itf).replace('/', '.'));
      }

      for (Object mn : classNode.methods)
      {
         MethodNode methodNode = (MethodNode) mn;

         Type type = Type.getType(methodNode.desc);
         fullDependencySet.add(type.getReturnType().getClassName());
         for (Type argType: type.getArgumentTypes())
         {
            fullDependencySet.add(argType.getClassName());
         }

         for (Object oo: methodNode.exceptions)
         {
            String excClass = (String) oo;
            fullDependencySet.add(excClass.replace('/', '.'));
         }

         if (null != methodNode.localVariables)
         {
            for (Object lon : methodNode.localVariables)
            {
               LocalVariableNode localVariableNode = (LocalVariableNode) lon;
               fullDependencySet.add(Type.getType(localVariableNode.desc).getClassName());
            }
         }
      }

      fullDependencySet.remove("void");
      fullDependencySet.remove("long");
      fullDependencySet.remove("int");
      fullDependencySet.remove("boolean");

      // the constructor adds it
      fullDependencySet.remove(packageName + '.' + className);
   }

   @Override
   public boolean equals(Object o)
   {
      if (null != o)
      {
         if (o instanceof ClassRef)
         {
            ClassRef cr = (ClassRef) o;
            if (packageName.equals(cr.packageName) && className.equals(cr.className))
            {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public int hashCode()
   {
      return packageName.hashCode() ^ className.hashCode();
   }

   public String getClassName()
   {
      return packageName + '.' + className;
   }

   public String getPackageName()
   {
      return packageName;
   }


   public PackageRef getPackage()
   {
      return PackageRef.getPackage(packageName);
   }

   public Set<String> getDependenciesClassNames()
   {
      return fullDependencySet;
   }

   public Set<ClassRef> getDependencies() { return resolvableDependencies; }

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
      return packageName + '.' + className;
   }

   public static Map<String, ClassRef> loadClasses(ZipFile zipFile) throws IOException
   {
      HashMap<String, ClassRef> allClasses = new HashMap<>();

      Enumeration<? extends ZipEntry> en = zipFile.entries();
      while (en.hasMoreElements())
      {
         ZipEntry e = en.nextElement();
         String entryName = e.getName();
         if (entryName.endsWith(".class"))
         {
            InputStream stream = zipFile.getInputStream(e);
            ClassRef cr = new ClassRef(stream, zipFile.getName());

            allClasses.put(cr.getClassName(), cr);
         }
      }

      return allClasses;
   }

   public static void resolveDependencies(Map<String, ClassRef> allClasses)
   {
      for (ClassRef cr: allClasses.values())
      {
         cr.resolvableDependencies = new HashSet<>();
         for (String name : cr.getDependenciesClassNames())
         {
            ClassRef depClass = allClasses.get(name);
            if (null != depClass)
            {
               cr.resolvableDependencies.add(depClass);
               depClass.markDependedOn();
            }
         }
      }
   }

   public void markDependedOn()
   {
      dependedOnByCount++;
   }

   public int getDependedOnByCount()
   {
      return dependedOnByCount;
   }
}
