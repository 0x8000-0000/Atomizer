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

package net.signbit.tools.atomizer.export;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

import net.signbit.tools.atomizer.ClassRef;

/** Export in Tulip format
 *
 * http://tulip.labri.fr/TulipDrupal/?q=tlp-file-format
 */
public class Tulip
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      System.out.println("(tlp \"2.0\"");

      int vertexId = 1;

      System.out.print("(nodes ");

      for (ClassRef cr : allClasses.values())
      {
         cr.setVertexId(vertexId);

         System.out.print(vertexId);
         System.out.print(' ');

         vertexId ++;
      }

      System.out.println(")");

      int edgeId = 1;

      for (ClassRef cr : allClasses.values())
      {
         for (ClassRef or : cr.getDependencies())
         {
            StringBuilder sb = new StringBuilder();

            sb.append("(edge ");
            sb.append(edgeId ++);
            sb.append(' ');
            sb.append(cr.getVertexId());
            sb.append(' ');
            sb.append(or.getVertexId());
            sb.append(')');

            System.out.println(sb.toString());
         }
      }

      System.out.println("(property  0 string \"viewLabel\"");
      System.out.println("   (default \"\" \"\")");

      for (ClassRef cr : allClasses.values())
      {
         StringBuilder sb = new StringBuilder();

         sb.append("   (node ");
         sb.append(cr.getVertexId());
         sb.append(' ');
         sb.append('\"');
         sb.append(cr.getClassName());
         sb.append('\"');
         sb.append(')');
         System.out.println(sb.toString());
      }

      // end of node label property
      System.out.println(")");

      // end of file
      System.out.println(")");
   }
}
