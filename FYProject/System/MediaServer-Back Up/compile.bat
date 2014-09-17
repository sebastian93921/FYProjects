@javac MSKernel.java
@jar -cvmf META-INF\MANIFEST.MF MSKernel.jar *.class
@del *.class

@copy ..\Central\dist\Central.jar .\app\




@java -jar MSKernel.jar
@pause