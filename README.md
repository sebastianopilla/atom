# Atom

Atom is a weekend project to implement an opinionated dependency injection container in Java in the spirit of ATG Nucleus.

I call this project "opinionated" because it doesn't try to implement any JSR-330-style annotations nor constructor injection, however useful those things can be. Instead, the only available injection type is setter injection, implemented by introspecting Java Beans, just as ATG Nucleus has done from about the year 2000. The code and the configurations are decoupled, binding happens at runtime.

The dependencies are expressed by writing .properties files in a particular location, known as the CONFIGPATH. In Atom, the CONFIGPATH is a list of directories, called "layers", containing properties files: a component is the combination of (at least) one Java class and (at least) one properties file, and the location of the properties file relative to the root of the CONFIGPATH is the full name of the component.

To instantiate a component, write its Java class and then a properties file which references this class, like the following example:

    $class=atom.examples.ExampleComponent
    $scope=global

When your application requests a component from Atom, Atom reads the CONFIGPATH from left to right looking for properties files in the path you requested, layering them in a way that properties defined in the rightmost files override properties defined in the leftmost files.

All of this is well explained in the ATG Nucleus programming guide available at http://docs.oracle.com/cd/E55783_02/Platform.11-2/ATGPlatformProgGuide/html/s0201nucleusorganizingjavabeancompone01.html

To express a dependency, write the property value as the path of the other component's properties file, minus the .properties extension. For example, if I want to have a /test/TestObjectDependent component which depends on the /test/TestObject component, I would write something like the following:

/test/TestObjectDependent:

    $class=atom.examples.ExampleComponent
    $scope=global
    dependency=/test/TestObject

## Differences from ATG Nucleus:

* this is a library only with no dependencies on other libraries, you can use it in a web application and in a desktop application
* no GLOBAL.properties or CONFIG.properties implemented yet
* no partial path to components, only full path are allowed
* layers in the CONFIGPATH can only be directories, no JAR nor ZIP files
* there's no administration interface for the moment, but one is planned when a web add-on will be written

