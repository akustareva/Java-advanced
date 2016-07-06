#!/bin/bash

javac -cp ../../java-advanced-2016/artifacts/ImplementorTest.jar src/ru/ifmo/ctddev/kustareva/implementor/* -d .

touch Manifest
echo "Manifest-Version: 1.0" > Manifest
echo "Main-Class: ru.ifmo.ctddev.kustareva.implementor.ImplRunner" >> Manifest
echo "Class-Path: ../../java-advanced-2016/artifacts/ImplementorTest.jar" >> Manifest
jar cfm Implementor.jar Manifest ru/ifmo/ctddev/kustareva/implementor/*

rm -r ru
rm -f Manifest