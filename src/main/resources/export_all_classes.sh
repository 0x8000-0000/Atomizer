#!/bin/sh

java -cp ${the.classpath}:${project.build.directory}/${project.build.finalName}.${project.packaging} net.signbit.tools.atomizer.export.ExportFullDependencies $1 $2 $3
