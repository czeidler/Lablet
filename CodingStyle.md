<!---
Copyright 2015.
Distributed under the terms of the Creative Commons Attribution 4.0 International license.
 
Authors:
    Clemens Zeidler <czei002@aucklanduni.ac.nz>
-->

# Coding Style

When working on a project with multiple developer it becomes important that code is readable and consistent for all developers of that project.
For that reason your code should follow a certain coding style and should not stick out from the rest of the code base.

## Style Overview

1. Lines are not longer than 100 columns. Wrap your code if a line gets too long. This avoids automatic line wraps and horizontal scrolling.
+ Package names are lowercase and reverse the project domain, e.g. nz.ac.auckland.project.package
+ Class names are capital, e.g. class FooClass {};
+ Methods and variables are camelCase starting with a lowercase letter.
+ Variables do not have any prefixes. (Current IDEs automatic highlight member variables)
+ For code blocks use the Kernighan and Ritchie (K&K) bracket style. (No new line before { and new line before })
+ Interfaces start with an I, e.g. IListener.
+ Classes, Methods and variables should have descriptive names.

See the following code example for more detailed style rules:

    package org.example.package;
    
    import java.util.List;
    
    
    /**
    * Use java doc do document code.
    * 
    * Note, that there is one line between package and imports and two lines between
    * the last import and the first line of code.
    */
    interface IFoo {
        // hook methods start with on
        void onEvent();
    }
    
    public class FooClass implements IFoo {
        // use final key word whenever possible
        final private String fooBar;
        // define constant values with uppercase letters and underscores
        final static private String CONSTANT_VALUE = "value";
            
        // Use the K&K { bracket style. (No new line before { and new line before })
        public FooClass(String fooBar) {
            this.fooBar = fooBar;
        }
        
        public void doStuff(int parameter, List<String> list) {        
            // Don't put extra spaces in a () blocks but use exactly one space to
            // separate items in such a block. Use exactly one space after an if
            // statment.
            // For example, DON't do: if( parameter>20){ but do:
            if (parameter > 20) {
                String outPutString = "Hello " + fooBar;
                System.out.println(outPutString);
            }
            
            // Avoid curly brackets if the code block has only one line.
            if (parameter > 10)
                System.out.println(fooBar);
            
            // Formatting of for loops:
            for (int i = 0; i < list.size(); i++)
                System.out.println(list.get(i);
        }
                
        // Use the override annotation
        @Override
        public void onEvent() {
        
        }
    }

## Copyright Header
Use the following copyright header at the top of each file.
The copyright header is directly followed by the package.

    /*
    * Copyright 20XX-20XX.
    * Distributed under the terms of the X License.
    *
    * Authors:
    *      Name <mail@provider.org>
    *      Name 2 <mail2@provider.org>
    */
    package org.example.package

## Code documentation
Use java doc to document your code.
However, avoid useless comments like:

        // counter for the number of blocks
        private int blockCounter; 
        
        /**
         * Sets the timeout.
         *
         * @param timeout
         */
        private void setTimeout(long timeout) {
        }
    
These types of comments just clutter the code and make it unreadable.

## Committing Code
* Commit your code in small logical units.
* Add descriptive commit messages, e.g. what you have done, why you have done it...
* Don't commit dead, commented or debug code.
* Make sure your the code compiles after your changes.