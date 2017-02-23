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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Atomizer
{
   private static HashMap<String, ClassRef> localClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      loadClasses(zipFile);

      //displayAllClasses();
      List<ClassRef> buildOrder = computeBuildOrder();
      for (ClassRef cr: buildOrder)
      {
         System.out.println(cr.getClassName());
      }
   }

   private static void displayAllClasses()
   {
      for (ClassRef cr: localClasses.values())
      {
         System.out.println(cr.getClassName());
         for (String name : cr.getDependencies())
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
      localClasses = new HashMap<String, ClassRef>();

      Enumeration<? extends ZipEntry> en = zipFile.entries();
      while (en.hasMoreElements())
      {
         ZipEntry e = en.nextElement();
         String entryName = e.getName();
         if (entryName.endsWith(".class"))
         {
            InputStream stream = zipFile.getInputStream(e);
            ClassRef cr = new ClassRef(stream);

            localClasses.put(cr.getClassName(), cr);
         }
      }
   }

   private static ArrayList<ClassRef> computeBuildOrder()
   {
      ArrayList<ClassRef> result = new ArrayList<ClassRef>();

      for (ClassRef cr: localClasses.values())
      {
         cr.markNew();
      }

      for (ClassRef cr: localClasses.values())
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

         for (String dependentClassName: cr.getDependencies())
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
