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
import java.util.*;
import java.util.zip.ZipFile;

public class Atomizer
{
   private static Map<String, ClassRef> localClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      loadClasses(zipFile);

      displayAllClasses();

      ArrayList<HashSet<ClassRef>> connectedComponents = findConnectedComponents(localClasses.values());
      System.out.println("Found " + connectedComponents.size() + " connected components");
      for (HashSet<ClassRef> connComp: connectedComponents)
      {
         if (connComp.size() > 1)
         {
            System.out.print("   " + connComp.size() + " : ");
            for (ClassRef cr: connComp)
            {
               System.out.print(cr);
               System.out.print(' ');
            }
            System.out.println();
         }
      }
      System.out.println();

      /*
      List<ClassRef> buildOrder = computeBuildOrder(localClasses.values());
      for (ClassRef cr: buildOrder)
      {
         System.out.println(cr.getClassName());
      }
      */
   }

   private static void displayAllClasses()
   {
      System.out.println("Loaded " + localClasses.size() + " classes");
      for (ClassRef cr: localClasses.values())
      {
         System.out.println(cr.getClassName());
         for (String name : cr.getDependenciesClassNames())
         {
            if (localClasses.containsKey(name))
            {
               System.out.println(">  " + name);
            }
            else
            {
               System.out.println("   " + name);
            }
         }
      }
   }

   private static void loadClasses(ZipFile zipFile) throws IOException
   {
      localClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(localClasses);
   }

   private static ArrayList<HashSet<ClassRef>> findConnectedComponents(Collection<ClassRef> refs)
   {
      for (ClassRef cr: refs)
      {
         cr.markNew();
      }

      ArrayList<HashSet<ClassRef>> connectedComponents = new ArrayList<HashSet<ClassRef>>();

      HashSet<ClassRef> currentComponent = new HashSet<ClassRef>();

      for (ClassRef cr: refs)
      {
         if (cr.isNew())
         {
            System.out.println("Selected: " + cr);
            cr.markFinal();
            visitConnectedComponent(cr, currentComponent, 1);

            connectedComponents.add(currentComponent);
            currentComponent = new HashSet<ClassRef>();
         }
      }

      return connectedComponents;
   }

   private static void visitConnectedComponent(ClassRef cr, HashSet<ClassRef> currentComponent, int level)
   {
      currentComponent.add(cr);

      for (String dependentClassName: cr.getDependenciesClassNames())
      {
         ClassRef dependentClass = localClasses.get(dependentClassName);
         if (dependentClass != null)
         {
            if (dependentClass.isNew())
            {
               for (int ii = 0; ii < level; ii ++)
               {
                  System.out.print("  ");
               }
               System.out.println(dependentClass);
               dependentClass.markFinal();
               visitConnectedComponent(dependentClass, currentComponent, level + 1);
            }
         }
      }
   }

   private static ArrayList<ClassRef> computeBuildOrder(Collection<ClassRef> refs)
   {
      ArrayList<ClassRef> result = new ArrayList<ClassRef>();

      for (ClassRef cr: refs)
      {
         cr.markNew();
      }

      for (ClassRef cr: refs)
      {
         if (cr.isNew())
         {
            visit(cr, result);
         }
      }

      return result;
   }

   private static void visit(ClassRef cr, ArrayList<ClassRef> result)
   {
      if (cr.isTemporary())
      {
         throw new RuntimeException("Cycle found");
      }

      if (cr.isNew())
      {
         cr.markTemporary();

         for (String dependentClassName: cr.getDependenciesClassNames())
         {
            ClassRef dependentClass = localClasses.get(dependentClassName);
            if (dependentClass != null)
            {
               visit(dependentClass, result);
            }
         }

         cr.markFinal();
         result.add(cr);
      }
   }
}
